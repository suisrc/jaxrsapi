package com.suisrc.jaxrsapi.core.factory;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jdeparser.JAnnotation;
import org.jboss.jdeparser.JAssignableExpr;
import org.jboss.jdeparser.JBlock;
import org.jboss.jdeparser.JCall;
import org.jboss.jdeparser.JCatch;
import org.jboss.jdeparser.JDocComment;
import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JIf;
import org.jboss.jdeparser.JJClass;
import org.jboss.jdeparser.JJField;
import org.jboss.jdeparser.JJMethod;
import org.jboss.jdeparser.JMethodDef;
import org.jboss.jdeparser.JMod;
import org.jboss.jdeparser.JParamDeclaration;
import org.jboss.jdeparser.JTry;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JTypes;
import org.jboss.jdeparser.JVarDeclaration;

import com.suisrc.core.exception.NoSupportException;
import com.suisrc.core.jdejst.JdeJst;
import com.suisrc.core.utils.CdiUtils;
import com.suisrc.jaxrsapi.core.ApiActivator;
import com.suisrc.jaxrsapi.core.ApiActivatorIndex;
import com.suisrc.jaxrsapi.core.JaxrsConsts;
import com.suisrc.jaxrsapi.core.ServiceClient;
import com.suisrc.jaxrsapi.core.annotation.LocalProxy;
import com.suisrc.jaxrsapi.core.annotation.NonProxy;
import com.suisrc.jaxrsapi.core.annotation.OneTimeProxy;
import com.suisrc.jaxrsapi.core.annotation.RemoteApi;
import com.suisrc.jaxrsapi.core.annotation.Retry;
import com.suisrc.jaxrsapi.core.annotation.Reviser;
import com.suisrc.jaxrsapi.core.annotation.TfDefaultValue;
import com.suisrc.jaxrsapi.core.annotation.Value;
import com.suisrc.jaxrsapi.core.proxy.ProxyBuilder;
import com.suisrc.jaxrsapi.core.runtime.RetryPredicate;
import com.suisrc.jaxrsapi.core.runtime.ReviseHandler;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * 生成执行的代理实体 使用javassist对java代码进行动态生成。
 * 
 * 同时引入JDeparser对代码进行可视化修改
 * 
 * JDeparser为sun公司的tools组件，这里使用的是jboss修正后的版本
 * 
 * 通过该工厂类，可以对接口的实现完成动态运行时实现和静态编译前实现。
 * 
 * @author Y13
 *
 */
public class ClientServiceFactory {

    /**
     * 创建接口实现
     * @param activator 激活器
     * @param index 代码索引
     * @param jjc 代码构建器
     * @param classInfo 接口信息
     * @return
     * @throws Exception
     */
    private void createImpl(ApiActivator activator, IndexView index, JJClass jjc, ClassInfo classInfo) throws Exception {
        createClassInfo(activator, jjc, classInfo);
        for (MethodInfo methodInfo : classInfo.methods()) {
            if (isProxyMethod(methodInfo)) {
                jjc._import(methodInfo.returnType().name().toString()); // 导入返回值类型
                createMethodInfo(activator, index, jjc, methodInfo);
            }
        }
    }

    /**
     * 是否需要进行代理 判断是否需要执行代理
     * 
     * @param info
     * @return
     */
    private boolean isProxyMethod(MethodInfo methodInfo) {
        if (methodInfo.name().equals("<init>") || methodInfo.name().startsWith("as")) {
            // || methodInfo.hasAnnotation(DotName.createSimple(NonProxy.class.getName()))) {
            return false; // 一些初始化和构造方法
        }
        return methodInfo.hasAnnotation(DotName.createSimple(GET.class.getCanonicalName()))
                || methodInfo.hasAnnotation(DotName.createSimple(POST.class.getCanonicalName()))
                || methodInfo.hasAnnotation(DotName.createSimple(PUT.class.getCanonicalName()))
                || methodInfo.hasAnnotation(DotName.createSimple(DELETE.class.getCanonicalName()));
    }

    /**
     * 构建代理的方法
     * 
     * @param index
     * @param jjc
     * @param method
     * @throws CannotCompileException
     * @throws NotFoundException
     * @throws ClassNotFoundException
     */
    private void createMethodInfo(ApiActivator activator, IndexView index, JJClass jjc, MethodInfo method)
            throws CannotCompileException, ClassNotFoundException, NotFoundException {
        // 创建方法
        JJMethod jjm = jjc.method(JMod.PUBLIC, method.returnType().name().toString(), method.name());
        // 获取方法的描述
        JMethodDef methodDef = jjm.getJMethodDef();
        jjc._import(Override.class);
        jjm.annotate(Override.class);
        methodDef.docComment().text("接口实现");
        // 参数列表
        List<Type> parameters = method.parameters();
        String ppname = "pm";
        List<JParamDeclaration> params = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++ ) {
            String type = parameters.get(i).name().toString();
            jjc._import(type);
            JParamDeclaration param = methodDef.param(type, ppname + i);
            params.add(param);
        }
        
        JBlock body = methodDef.body();
        // -----------------------------------------------------------------------------------ZERO 参数获取部分
        // 获取方法上的注解（这些注解标记在方法的参数上）
        List<AnnotationInstance> annos_m = method.annotations();
        // 分类
        List<AnnotationInstance> value_ms = new ArrayList<>();
        List<AnnotationInstance> defaultValue_ms = new ArrayList<>();
        Map<Short, AnnotationInstance> tfDefaultValue_ms = new HashMap<>();
        List<AnnotationInstance> reviser_ms = new ArrayList<>();
        AnnotationInstance reviser_m = null;
        // 对应的重写赋值内容 
        List<Consumer<JBlock>> retryValue_lst = new ArrayList<>();
        
        for (AnnotationInstance anno : annos_m) {
            String annoName = anno.name().toString();
            if (annoName.equals(NonProxy.class.getName())) {
                // NonProxy
                AnnotationValue ave = anno.value();
                Boolean stauts = ave == null ? true : activator.getAdapter(ave.asString(), Boolean.class);
                if (stauts != null && stauts) {
                    // 不再记性下面的内容构建，相当于禁用了该接口
                    jjc._import(NoSupportException.class);
                    JCall jcall = JTypes.typeOf(NoSupportException.class)._new();
                    jcall.arg(JExprs.str("The interface has been disabled, Config:[" + (ave == null ? "Permanently Disabled" : ave.asString()) + "]"));
                    body._throw(jcall);
                    return;
                }
            }
            if (anno.target().kind() == Kind.METHOD && annoName.equals(Reviser.class.getName())) {
                reviser_m = anno;
                continue;
            }
            if (anno.target().kind() != Kind.METHOD_PARAMETER) {
                continue;
            }
            if (annoName.equals(Value.class.getName())) {
                // Value
                value_ms.add(anno);
            } else if (annoName.equals(DefaultValue.class.getName())) {
                // DefaultValue
                defaultValue_ms.add(anno);
            } else if (annoName.equals(TfDefaultValue.class.getName())) {
                // TfDefaultValue
                short position = anno.target().asMethodParameter().position();
                tfDefaultValue_ms.put(position, anno);
            } else if (annoName.equals(Reviser.class.getName())) {
                // Reviser
                reviser_ms.add(anno);
            }
        }
        // -----------------------------------------------------------------------------------ZERO parameter Value参数获取部分
        for (AnnotationInstance anno : value_ms) { // Value
            JCall methodExpr = getValueMethodExpr(jjm, anno);
            // 特殊处理
            short position = anno.target().asMethodParameter().position();
            JParamDeclaration param = params.get(position);
            methodExpr.arg(param.type().field("class"));
            //-----
            AnnotationValue retryValue = anno.value("retry");
            if (retryValue != null && retryValue.asBoolean()) {
                AnnotationValue reoverValue = anno.value("reover");
                boolean reover = reoverValue != null ? reoverValue.asBoolean() : true;
                retryValue_lst.add(block -> createParamValueInfo(jjm, block, params, anno, methodExpr, reover));
            }
            createParamValueInfo(jjm, body, params, anno, methodExpr, false);
        }
        // -----------------------------------------------------------------------------------ZERO parameter DefaultValue参数获取部分
        for (AnnotationInstance anno : defaultValue_ms) { // DefaultValue
            short position = anno.target().asMethodParameter().position();
            JCall methodExpr = getDefaultValueMethodExpr(jjm, anno, tfDefaultValue_ms.get(position), parameters.get(position));
            createParamValueInfo(jjm, body, params, anno, methodExpr, false);
        }
        // -----------------------------------------------------------------------------------ZERO field 参数获取部分
        // 参数内部的属性
        for (int i = 0; i < parameters.size(); i++) {
            Type paramType = parameters.get(i);
            // 查找参数的定义
            ClassInfo classInfo = index.getClassByName(paramType.name());
            if (classInfo == null) {
                continue;
            }
            // -----------------------------------------------------------------------------------ZERO field ThreadValue参数获取部分
            List<AnnotationInstance> annos_f = classInfo.annotations().get(DotName.createSimple(Value.class.getName()));
            if (annos_f != null && !annos_f.isEmpty()) {
                for (AnnotationInstance anno : annos_f) { // Value
                    JCall methodExpr = getValueMethodExpr(jjm, anno);
                    FieldInfo fieldInfo = anno.target().asField();
                    String type = fieldInfo.type().name().toString();
                    jjc._import(type);
                    methodExpr.arg(JTypes.typeNamed(type).field("class"));
                    JParamDeclaration param = params.get(i);
                    //-----
                    AnnotationValue retryValue = anno.value("retry");
                    if (retryValue != null && retryValue.asBoolean()) {
                        AnnotationValue reoverValue = anno.value("reover");
                        boolean reover = reoverValue != null ? reoverValue.asBoolean() : true;
                        retryValue_lst.add(block -> createFieldValueInfo(jjm, block, param, anno, methodExpr, reover));
                    }
                    createFieldValueInfo(jjm, body, param, anno, methodExpr, false);
                }
            }
            // -----------------------------------------------------------------------------------ZERO field DefaultValue参数获取部分
            annos_f = classInfo.annotations().get(DotName.createSimple(DefaultValue.class.getName()));
            if (annos_f != null && !annos_f.isEmpty()) {
                for (AnnotationInstance anno : annos_f) { // DefaultValue
                    AnnotationInstance tfAnno = null;
                    FieldInfo fieldInfo = anno.target().asField();
                    for ( AnnotationInstance ai : fieldInfo.annotations()) {
                        if (ai.name().toString().equals(TfDefaultValue.class.getName())) {
                            tfAnno = ai;
                            break;
                        }
                    }
                    JCall methodExpr = getDefaultValueMethodExpr(jjm, anno, tfAnno, fieldInfo.type());
                    createFieldValueInfo(jjm, body, params.get(i), anno, methodExpr, false);
                }
            }
            // -------------------------------------------------------------------------------ZERO 最后的数据修正拦截
            annos_f = classInfo.annotations().get(DotName.createSimple(Reviser.class.getName()));
            if (annos_f != null && !annos_f.isEmpty()) {
                for (AnnotationInstance anno : annos_f) { // reviser
                    JCall methodExpr = getReviserMethodExpr(jjm, anno);
                    createFieldReviserInfo(jjm, body, params.get(i), anno, methodExpr);
                }
            }
        }
        // -------------------------------------------------------------------------------最后的数据修正拦截
        for (AnnotationInstance anno : reviser_ms) { // reviser
            JCall methodExpr = getReviserMethodExpr(jjm, anno);
            short position = anno.target().asMethodParameter().position();
            createParamReviserInfo(jjm, body, params.get(position), anno, methodExpr);
        }
        // -------------------------------------------------------------------------------ZERO 返回值处理
        createReturnInfo(jjm, body, method, params, reviser_m, retryValue_lst);
        // -------------------------------------------------------------------------------ZERO 异常处理
        if (method.exceptions() != null && !method.exceptions().isEmpty()) {
            for (Type type : method.exceptions()) {
                methodDef._throws(type.name().toString());
            }
        }
    }

    /**
     * 获取拦截器中的内容
     * 
     * 没有给参数，参数这里无法给出
     * @param anno
     * @return
     */
    private JCall getReviserMethodExpr(JJMethod jjm, AnnotationInstance anno) {
        // Reviser.class
        String clazz = anno.value().asClass().name().toString();
        AnnotationValue ave = anno.value("master");
        String master = ave == null ? JaxrsConsts.NONE : ave.asString();
        // 构建代码
        jjm.getJJClass()._import(clazz);
        JCall reviseHandler = JTypes.typeNamed(clazz)._new();
        if (!master.isEmpty()) {
            reviseHandler.arg(JExprs.$v(master));
        }
        JCall methodExpr = reviseHandler.call(ReviseHandler.METHOD);
        return methodExpr;
    }

    /**
     * 创建参数的调用
     * @param body
     * @param params
     * @param anno
     * @param methodExpr
     */
    private void createParamReviserInfo(JJMethod jjm, JBlock body, JParamDeclaration param, AnnotationInstance anno, JCall methodExpr) {
        JAssignableExpr paramJExpr = JExprs.$v(param);
        methodExpr.arg(paramJExpr);
        body.assign(paramJExpr, methodExpr);
    }

    /**
     * 创建参数中属性的调用
     * @param body
     * @param param
     * @param anno
     * @param methodExpr
     */
    private void createFieldReviserInfo(JJMethod jjm, JBlock body, JParamDeclaration param, AnnotationInstance anno, JCall methodExpr) {
        FieldInfo fieldInfo = anno.target().asField();
        String name = fieldInfo.name();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        String getMethod = "get" + name;
        String setMethod = "set" + name;
        if (fieldInfo.declaringClass().method(getMethod) == null) {
            name = "is" + name;
        }
        JAssignableExpr paramVar = JExprs.$v(param);
        JCall paramGet = paramVar.call(getMethod);
        JCall paramSet = paramVar.call(setMethod);
        methodExpr.arg(paramGet);
        paramSet.arg(methodExpr);
        body.add(paramSet);
    }

    /**
     * 获取默认属性赋值方式
     * @param anno
     * @param TfDefaultValue_ms
     * @param parameters
     * @return
     */
    private JCall getDefaultValueMethodExpr(JJMethod jjm, AnnotationInstance anno, AnnotationInstance tfAnno, Type paramType) {
        // DefaultValue.class
        // TfDefaultValue.class
        String type = tfAnno != null ? tfAnno.value().asString() : paramType.name().toString();
        String value = anno.value().asString();
        // 赋值方法
        jjm.getJJClass()._import(Transform.class);
        jjm.getJJClass()._import(type);
        JCall methodExpr = JExprs.callStatic(Transform.class, Transform.METHOD);
        methodExpr.arg(JTypes.typeNamed(type).field("class"));
        methodExpr.arg(JExprs.str(value));
        return methodExpr;
    }

    /**
     * 获取常规属性赋值方式
     * @param anno
     * @return
     */
    private JCall getValueMethodExpr(JJMethod jjm, AnnotationInstance anno) {
        // String actname = ServiceClient.MED_getActivator; // 获取activator方法
        // Value.class
        String actname = JaxrsConsts.FIELD_ACTIVATOR; // 获取activator属性
        String metname = ServiceClient.MED_getAdapter;
        String value = anno.value().asString();
        // 赋值方法
        JAssignableExpr actField = JExprs.$v(actname);
        JCall methodExpr = actField.call(metname);
        methodExpr.arg(JExprs.str(value));
        return methodExpr;
    }

    /**
     * 创建参数属性赋值
     * @param body
     * @param param
     * @param anno
     * @param methodExpr
     */
    private void createParamValueInfo(JJMethod jjm, JBlock body, List<JParamDeclaration> params, AnnotationInstance anno, JCall methodExpr, boolean override) {
        short position = anno.target().asMethodParameter().position();
        JParamDeclaration param = params.get(position);
        // 获取参数
        JAssignableExpr paramVar = JExprs.$v(param);
        if (override) {
            body.assign(paramVar, methodExpr);
        } else {
            // 判断参数是否为空
            JIf jif = body._if(paramVar.eq(JExpr.NULL));
            // 对参数进行赋值
            JVarDeclaration temp = jif.var(0, param.type(), "temp", methodExpr.cast(param.type()));
            JAssignableExpr tempVar = JExprs.$v(temp);
            JIf jif2 = jif._if(tempVar.ne(JExpr.NULL));
            jif2.assign(paramVar, JExprs.$v(temp));
        }
    }

    /**
     * 创建参数属性内容赋值
     * @param body
     * @param param
     * @param anno
     * @param methodExpr
     * @param override 是否强制覆盖
     */
    private void createFieldValueInfo(JJMethod jjm, JBlock body, JParamDeclaration param, AnnotationInstance anno, JCall methodExpr, boolean override) {
        FieldInfo fieldInfo = anno.target().asField();
        String name = fieldInfo.name();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        String getMethod = "get" + name;
        String setMethod = "set" + name;
        if (fieldInfo.declaringClass().method(getMethod) == null) {
            name = "is" + name;
        }
        String type = fieldInfo.type().name().toString();
        JAssignableExpr paramVar = JExprs.$v(param);
        JCall paramGet = paramVar.call(getMethod);
        JCall paramSet = paramVar.call(setMethod);
        
        if (override) {
            paramSet.arg(methodExpr);
            body.add(paramSet);
        } else {
            JIf jif = body._if(paramGet.eq(JExpr.NULL));
            JVarDeclaration temp = jif.var(0, type, "temp", methodExpr.cast(type));
            JAssignableExpr tempVar = JExprs.$v(temp);
            paramSet.arg(tempVar);
            JIf jif2 = jif._if(tempVar.ne(JExpr.NULL));
            jif2.add(paramSet);
        }
    }

    /**
     * 构建返回值内容
     * @param jjm
     * @param body
     * @param method
     * @param params
     * @param retrylst
     */
    private void createReturnInfo(JJMethod jjm, JBlock body, MethodInfo method, List<JParamDeclaration> params, 
            AnnotationInstance reviser, List<Consumer<JBlock>> retrylst) {
        // 本地代理请求注解
        AnnotationInstance anno = method.annotation(DotName.createSimple(LocalProxy.class.getName()));
        // 数据请求方法
        JCall methodExpr = anno != null ? createLocalProxyReturnInfo(jjm, method, anno) : createRemoteProxyResultInfo(jjm, body, method);
        // 给出方法的参数
        for (JParamDeclaration param : params) {
            methodExpr.arg(JExprs.$v(param));
        }
        // 对方返回值内容是否需要进行处理
        if (reviser != null) {
            JCall reviserExpr = getReviserMethodExpr(jjm, reviser);
            methodExpr = reviserExpr.arg(methodExpr);
        }
        // 对于请求的内容，是否支持重试
        anno = method.annotation(DotName.createSimple(Retry.class.getName()));
        JExpr result = anno == null ? methodExpr.cast(method.returnType().name().toString()) : getRetryExpr(jjm, body, method, methodExpr, anno, retrylst);
        // 设定返回值
        body._return(result);
    }

    /**
     * 获取重试内容的代码片段
     * 
     *  RetryPredicateImpl predicate = new RetryPredicateImpl();
     *  int count = 2;
     *  String result;
     *  Exception exception;
     *  do {
     *      result = null;
     *      exception = null;
     *      try {
     *          result = (String)proxy.getApi_1(pm0);
     *      } catch (Exception e) {
     *          exception = e;
     *      }
     *  } while (predicate.test(2, --count, result, exception) && count > 0);
     *  return result;
     *  
     * 没有给参数，参数这里无法给出
     */
    private JExpr getRetryExpr(JJMethod jjm, JBlock body, MethodInfo method, JCall jcm, AnnotationInstance anno, List<Consumer<JBlock>> retrylst) {
        // Retry.class
        String clazz = anno.value().asClass().name().toString(); // 类型
        AnnotationValue ave = anno.value("master"); // 初始化构造时候，使用的构造参数
        String master = ave == null ? JaxrsConsts.NONE : ave.asString();
        ave = anno.value("count");
        int count = ave == null ? 2 : ave.asInt();
        // 构建代码
        jjm.getJJClass()._import(clazz);
        JType testType = JTypes.typeNamed(clazz); //断言类型
        JCall testNew = testType._new();
        if (!master.isEmpty()) {
            testNew.arg(JExprs.$v(master));
        }
        jjm.getJJClass()._import(Exception.class);
        
        JExpr countHex = JExprs.hex(count);
        
        JVarDeclaration predicateVar = body.var(0, testType, "predicate", testNew);
        JVarDeclaration countVar = body.var(0, JType.INT, "count", countHex);
        JVarDeclaration resultVar = body.var(0, JTypes.typeNamed(method.returnType().name().toString()), "result");
        JVarDeclaration exceptionVar = body.var(0, JTypes.typeOf(Exception.class), "exception");

        JAssignableExpr predicateExpr = JExprs.$v(predicateVar);
        JAssignableExpr countExpr = JExprs.$v(countVar);
        JAssignableExpr resultExpr = JExprs.$v(resultVar);
        JAssignableExpr exceptionExpr = JExprs.$v(exceptionVar);
        
        JCall testCall = predicateExpr.call(RetryPredicate.METHOD);
        testCall.arg(countHex);
        testCall.arg(countExpr.preDec());
        testCall.arg(resultExpr);
        testCall.arg(exceptionExpr);
        JBlock doBlock = body._do(testCall.and(countExpr.gt(JExpr.ZERO)));

        doBlock.assign(resultExpr, JExpr.NULL);
        doBlock.assign(exceptionExpr, JExpr.NULL);
        JTry doTry = doBlock._try();
        if (!retrylst.isEmpty()) {
            JIf jif = doTry._if(countExpr.ne(countHex));
            retrylst.forEach(c -> c.accept(jif));
        }
        doTry.assign(resultExpr, jcm);
        JCatch doCatch = doTry._catch(0, Exception.class, " e"); // 此处与bug, 必须是“ e”, 否则生成的代码有问题
        doCatch.assign(exceptionExpr, JExprs.$v("e"));
        
        return JExprs.$v(resultVar);
    }

    /**
     * 获取远程返回请求代理返回值
     * @param jjm
     * @param body
     * @param method
     * @return
     */
    private JCall createRemoteProxyResultInfo(JJMethod jjm, JBlock body, MethodInfo method) {
        JCall methodExpr;
        if (jjm.getJJClass().isOneTimeProxy()) {
            String proxyType = method.declaringClass().name().toString();
            
            jjm.getJJClass()._import(WebTarget.class);
            jjm.getJJClass()._import(ProxyBuilder.class);
            JCall targetCall = JExprs.$v(JaxrsConsts.FIELD_ACTIVATOR).call(ServiceClient.MED_getAdapter);
            targetCall.arg(JExpr.NULL.cast(String.class));
            targetCall.arg(JTypes.typeOf(WebTarget.class).field("class"));
            JVarDeclaration target = body.var(0, WebTarget.class, "target", targetCall.cast(WebTarget.class));
            JCall proxyExpr = JExprs.callStatic(ProxyBuilder.class, "builder");
            proxyExpr.arg(JTypes.typeNamed(proxyType).field("class"));
            proxyExpr.arg(JExprs.$v(target));
            JCall buildExpr = proxyExpr.call("build");
            
            JAssignableExpr proxy = JExprs.$v(body.var(0, proxyType, "proxy", buildExpr));
            methodExpr = proxy.call(method.name());
        } else {
            methodExpr = JExprs.$v(JaxrsConsts.FIELD_PROXY).call(method.name());
        }
        return methodExpr;
    }

    /**
     * 获取本地代理请求返回值
     * @param jjm
     * @param method
     * @param anno
     * @return
     */
    private JCall createLocalProxyReturnInfo(JJMethod jjm, MethodInfo method, AnnotationInstance anno) {
        String className = anno.value().asClass().name().toString(); // 代理的类型
        AnnotationValue annoValue = anno.value("master");
        String construct = annoValue == null ? JaxrsConsts.NONE : annoValue.asString();
        annoValue = anno.value("method");
        String methodName = annoValue == null ? LocalProxy.defaultMethod : annoValue.asString();
        // 赋值
        jjm.getJJClass()._import(className);
        JCall proxy = JTypes.typeNamed(className)._new();
        if (!construct.isEmpty()) {
            proxy.arg(JExprs.$v(construct));
        }
        JCall methodExpr = proxy.call(methodName);
         JCall baseUrl = JExprs.$v(JaxrsConsts.FIELD_ACTIVATOR).call("getBaseUrl");
         JExpr path = baseUrl;
         if (jjm.getJJClass().getRootPath() != null) {
             path = path.plus(JExprs.str("/" + jjm.getJJClass().getRootPath()));
         }
         methodExpr.arg(path.plus(JExprs.str(getMethodUri(method))));
        return methodExpr;
    }

    /**
     * 获取uri
     */
    private String getMethodUri(MethodInfo method) {
        String path = "";
        DotName pathAnnoName = DotName.createSimple(Path.class.getName());
        for (AnnotationInstance ai : method.declaringClass().classAnnotations()) {
            if (ai.name().equals(pathAnnoName)) {
                path += "/" + ai.value().asString();
                break;
            }
        }
        AnnotationInstance ai = method.annotation(pathAnnoName);
        if (ai != null) {
            path += "/" + ai.value().asString();
        }
        if (!path.isEmpty()) {
            path = path.replaceAll("/{2,}", "/");
            if (path.charAt(path.length() - 1) == '/') {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

    /**
     * 构建基础信息
     * 
     * @param ctPool
     * @param classInfo
     * @param named
     * @param ctClass
     * @throws NotFoundException
     * @throws ClassNotFoundException
     * @throws CannotCompileException
     */
    private void createClassInfo(ApiActivator activator, JJClass jjc, ClassInfo classInfo)
            throws NotFoundException, ClassNotFoundException, CannotCompileException {
        String activatorName = null;
        { // 获取激活器的名字
            Named named = activator.getClass().getAnnotation(Named.class);
            if (named == null) {
                    throw new RuntimeException("Not found 'Named' annotation: " + activator.getClass());
            } else {
                activatorName = named.value();
            }
        }
        String api = classInfo.name().toString();
        // 注释说明
        JDocComment comment = jjc.getJClassDef().docComment();
        comment.text("Follow the implementation of the restful 2.0 standard remote access agent.");
        comment.htmlTag("see", true).text("https://suisrc.github.io/jaxrsapi");
        comment.htmlTag("generateBy", true).text(ClientServiceFactory.class.getCanonicalName());
        comment.htmlTag("time", true).text(LocalDateTime.now().toString());
        comment.htmlTag("author", true).text("Y13");
        // 实现代理的注解
        jjc._import(ApplicationScoped.class);
        JAnnotation anno = jjc.annotate(ApplicationScoped.class);
        if (activator.isMulitMode()) { // 单接口多服务器模式
            jjc.setMulitMode(true);
            jjc._import(Named.class);
            anno = jjc.annotate(Named.class);
            jjc.annotateValue(anno, "value", activatorName + JaxrsConsts.separator + classInfo.name().toString());
        }
        // 继承的接口
        jjc._import(api);
        jjc._import(ServiceClient.class);
        
        jjc._implements(api);
        jjc._implements(ServiceClient.class);
        // 设定是否为一次性访问接口
        jjc.setOneTimeProxy(classInfo.annotations().containsKey(DotName.createSimple(OneTimeProxy.class.getName())));
        JVarDeclaration proxyField = null;
        if (!jjc.isOneTimeProxy()) {
            // 远程访问控制器
            JJField pf = jjc.field(JMod.PRIVATE, api, JaxrsConsts.FIELD_PROXY);
            proxyField = pf.getJVarDeclaration();
            proxyField.blockComment().text("远程代理访问客户端控制器");
        }
        jjc._import(ApiActivator.class);
        JJField af = jjc.field(JMod.PRIVATE, ApiActivator.class, JaxrsConsts.FIELD_ACTIVATOR);
        JVarDeclaration activatorField = af.getJVarDeclaration();
        activatorField.blockComment().text("远程服务器控制器，具有服务器信息");

        // initialize方法
        JJMethod im = jjc.method(JMod.PUBLIC, void.class, ServiceClient.MED_initialize);
        JMethodDef initializeMethod = im.getJMethodDef();
        initializeMethod.docComment().text("初始化");
        // 构建实现部分
        JBlock initializeBody = initializeMethod.body();
        if (!jjc.isOneTimeProxy()) {
            // 0170915 PostConstruct和@Inject存在不同步情况， 更改为在ServiceClient.MED_setActivator进行初始化操作
            //jjc._import(PostConstruct.class);
            //anno = initializeMethod.annotate(PostConstruct.class);
            jjc._import(WebTarget.class);
            jjc._import(ProxyBuilder.class);
            JCall targetCall = JExprs.$v(activatorField).call(ServiceClient.MED_getAdapter);
            targetCall.arg(JExpr.NULL.cast(String.class));
            targetCall.arg(JTypes.typeOf(WebTarget.class).field("class"));
            JExpr targetJExpr = targetCall.cast(WebTarget.class);
            // 0180205 增加对@RemoteApi的使用
            List<AnnotationInstance> annoIs = classInfo.annotations().get(DotName.createSimple(RemoteApi.class.getName()));
            if (annoIs != null && !annoIs.isEmpty()) {
                // 如果存在，有且仅有一个
                AnnotationInstance ai = annoIs.get(0);
                AnnotationValue av;
                String rp;
                if ((av = ai.value()) != null && !(rp = av.asString()).isEmpty()) {
                    jjc.setRootPath(rp);
                    targetJExpr = targetJExpr.call("path").arg(JExprs.str(rp));
                }
            }
            JVarDeclaration target = initializeBody.var(0, WebTarget.class, "target", targetJExpr);
            //JVarDeclaration target = initializeBody.var(0, WebTarget.class, "target", targetCall);
            JCall proxyExpr = JExprs.callStatic(ProxyBuilder.class, "builder");
            proxyExpr.arg(JTypes.typeNamed(api).field("class"));
            proxyExpr.arg(JExprs.$v(target));
            JCall buildExpr = proxyExpr.call("build");
            initializeBody.assign(JExprs.$v(proxyField), buildExpr);
        }
        
        // 激活器的get和set方法
        jjc.getter(af, ServiceClient.MED_getActivator, "获取远程服务器控制器");
        // jjc.setter(af, ServiceClient.MED_setActivator, "配置远程服务器控制器");
        jjc._import(Named.class);
        JJMethod am = jjc.method(JMod.PUBLIC, void.class, ServiceClient.MED_setActivator);
        anno = am.annotate(Named.class);
        am.annotateValue(anno, "value", activatorName);
        JMethodDef activatorMethod = am.getJMethodDef();
        activatorMethod.docComment().text("配置远程服务器控制器");
        JBlock activatorBody = activatorMethod.body();
        JExpr activatorVar;
        if (activator.isStdInject()) {
            // 标准JAVA注入接口
            jjc._import(Inject.class);
            anno = am.annotate(Inject.class);
            JParamDeclaration param = activatorMethod.param(activatorField.type(), "pm");
            activatorVar = JExprs.$v(param);
        } else {
            // 非标准接口时候，通过构造方法对该内容记性注入调用
            jjc._import(CdiUtils.class);
            JCall cdiExpr = JExprs.callStatic(CdiUtils.class, CdiUtils.MED_DEF_QUALIFIER);
            cdiExpr.arg(activatorField.type().field("class"));
            activatorVar = cdiExpr;
            // 需要在构造方法中调用ServiceClient.MED_setActivator方法
            JMethodDef constructorMethod = jjc.getJClassDef().constructor(JMod.PUBLIC);
            constructorMethod.docComment().text("构造方法");
            JBlock constructorBody = constructorMethod.body();
            constructorBody.call(ServiceClient.MED_setActivator);
        }
        activatorBody.assign(JExprs.$v(activatorField), activatorVar);
        // 判断参数是否为空
        JIf jif = activatorBody._if(JExprs.$v(activatorField).ne(JExpr.NULL));
        jif.call(ServiceClient.MED_initialize);
    }

    /**
     * 构建激活器的索引
     * @param activator
     * @param jj
     * @param map
     */
    @SuppressWarnings("rawtypes")
    private void createActivatorIndex(ApiActivator activator, JJClass jjc, Map<Object, JJClass> apiMap) {
        String api = ApiActivatorIndex.class.getCanonicalName();
        // 注释说明
        JDocComment comment = jjc.getJClassDef().docComment();
        comment.text("Restful api implementation index.");
        comment.htmlTag("see", true).text("https://suisrc.github.io/jaxrsapi");
        comment.htmlTag("generateBy", true).text(ClientServiceFactory.class.getCanonicalName());
        comment.htmlTag("time", true).text(LocalDateTime.now().toString());
        comment.htmlTag("author", true).text("Y13");
        // 配置接口信息
        jjc._import(api);
        jjc._implements(api);
        
        Named namedAnno = activator.getClass().getAnnotation(Named.class);
        String namedStr = namedAnno.value();
        
        for (Entry<Object, JJClass> entry : apiMap.entrySet()) {
            if (!(entry.getKey() instanceof Class)) {
                continue;
            }
            Class<?> apiClass = (Class)entry.getKey();
            
            String name = apiClass.getSimpleName();
            String apiName = namedStr + JaxrsConsts.separator + apiClass.getCanonicalName();
            String impName = entry.getValue().getCanonicalName();
            
            int mod = JMod.PUBLIC + JMod.STATIC + JMod.FINAL;
            jjc.field(mod, String.class, name, JExprs.str(apiName));
            jjc.field(mod, String.class, name + ApiActivatorIndex.IMPL, JExprs.str(impName));
        }
    }
    
    // ----------------------------------------------------------------------ZERO 构建入口

    /**
     * 处理递归创建
     * @param index
     * @param acceptThen
     * @param key
     * @param cai
     * @throws Exception
     */
    public static void processIndex(IndexView index, BiConsumer<Class<?>, CtClass> acceptThen, String key, boolean cai) throws Exception {
        Collection<ClassInfo> activatorClasses = index.getKnownDirectImplementors((DotName.createSimple(ApiActivator.class.getName())));
        Set<Class<?>> activatorSet = new HashSet<>();
        Set<Class<?>> subclasses = new HashSet<>(); // 用于判断是否为其他实体的继承
        for (ClassInfo activatorClass : activatorClasses) {
            try {// 加载对象
                Class<?> classActivator = (Class<?>) Class.forName(activatorClass.name().toString());
                activatorSet.add(classActivator);
                subclasses.add(classActivator.getSuperclass());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (Class<?> classActivator : activatorSet) {
            if (subclasses.contains(classActivator)) {
                continue;
            } // 有激活器继承该激活器，舍弃不予加载
            if (classActivator.isInterface() || Modifier.isAbstract(classActivator.getModifiers())) {
                continue;
            }
            // 有的时候，该内容是被临时使用的，没有任何意义
            ApiActivator activator = (ApiActivator) classActivator.newInstance();
            ClientServiceFactory.createImpl(activator, index, acceptThen, key, cai);
        }
    }
    
    /**
     * 创建接口实体
     * @param activator
     * @param index
     * @param acceptThen
     * @param key
     * @param cai
     * @throws Exception
     */
    static void createImpl(ApiActivator activator, IndexView index, BiConsumer<Class<?>, CtClass> acceptThen, String key, boolean cai) throws Exception {
        createImpl(activator, index, new JdeJst(), jj -> {
            Map<Object, CtClass> ctClasses = jj.writeSource();
            ctClasses.entrySet().forEach(v -> acceptThen.accept((Class<?>)v.getKey(), v.getValue()));
        }, key != null ? key : "$$jaxrsapi", cai);
    }

    /**
     * 创建接口实体
     * @param activator
     * @param index
     * @param targetFile
     * @param key
     * @param cai
     * @throws Exception
     */
    static void createImpl(ApiActivator activator, IndexView index, String targetFile, String key, boolean cai) throws Exception {
        JdeJst jjst = new JdeJst();
        jjst.setTarget(new File(targetFile));
        jjst.setShowSrc(true); // 需要输出文件
        createImpl(activator, index, jjst, jj -> jj.writeSource4File(), key != null ? key : "_jaxrsapi", cai);
    }

    /**
     * 
     * 创建接口实体
     * 
     * 主要接口，其他createImpl都会汇集到该接口中。
     * 
     * @param activator
     * @param index
     * @param jj
     * @param accpetThen
     * @param key
     * @param isCreateActivatorIndex 是否构建激活器的索引
     * @throws Exception
     */
    static void createImpl(ApiActivator activator, IndexView index, JdeJst jj, Consumer<JdeJst> accpetThen, 
            String key, boolean isCreateActivatorIndex) throws Exception {
        ClientServiceFactory factory = new ClientServiceFactory();
        try {
            
            for (Class<?> apiClass : activator.getClasses()) {
                ClassInfo info = index.getClassByName(DotName.createSimple(apiClass.getName()));
                // 生成api代理实体
                String name = apiClass.getCanonicalName() + key;
                JJClass jjc = jj.createClass(apiClass, name);
                factory.createImpl(activator, index, jjc, info);
            }
            if (isCreateActivatorIndex) {
                Map<Object, JJClass> classMap = jj.getClassMap();
                // 构建激活器的索引
                String name = activator.getClass().getSimpleName() + JaxrsConsts.ACTIVATOR_INDEX_SUFFIX;
                String pkgName = activator.getClass().getPackage().getName();
                JJClass jjc = jj.createClass(ApiActivatorIndex.class, name, pkgName);
                // 强制修改包位置为激活器位置相同
                factory.createActivatorIndex(activator, jjc, classMap);
            }
            accpetThen.accept(jj);
        } finally {
            jj.destory();// 清理
        }
    }

    /**
     * 构建Index
     * @param result
     * @param loader
     * @param clazzes
     * @return
     */
    @SuppressWarnings("unchecked")
    static IndexView createIndexer(List<ApiActivator> result, ClassLoader loader, 
            Class<? extends ApiActivator>... clazzes) {
        Indexer indexer = new Indexer();
        try {
            Set<Class<?>> useClasses = new HashSet<>();
            for (Class<? extends ApiActivator> activatorClass : clazzes) {
                ApiActivator activator = activatorClass.newInstance();
                result.add(activator); // 放入缓存, 用于外部回调使用
                // 解析需要处理的接口内容
                for (Class<?> apiClass : activator.getClasses()) {
                    useClasses.add(apiClass);
                    for (Method method : apiClass.getMethods()) {
                        for (Class<?> paramType : method.getParameterTypes()) {
                            if (!paramType.isPrimitive()) {
                                useClasses.add(paramType);
                            }
                        }
                    }
                }
            }
            for (Class<?> clazz : useClasses) {
                InputStream is = loader.getResourceAsStream(clazz.getName().replace('.', '/') + ".class");
                indexer.index(is);
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return indexer.complete();
    }
}
