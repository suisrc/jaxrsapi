package com.suisrc.jaxrsapi.core.factory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jdeparser.JAnnotation;
import org.jboss.jdeparser.JAssignableExpr;
import org.jboss.jdeparser.JBlock;
import org.jboss.jdeparser.JCall;
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
import org.jboss.jdeparser.JTypes;
import org.jboss.jdeparser.JVarDeclaration;

import com.suisrc.core.ScCDI;
import com.suisrc.core.exception.NoSupportException;
import com.suisrc.core.jdejst.JdeJst;
import com.suisrc.jaxrsapi.core.ApiActivator;
import com.suisrc.jaxrsapi.core.Consts;
import com.suisrc.jaxrsapi.core.ServiceClient;
import com.suisrc.jaxrsapi.core.annotation.LocalProxy;
import com.suisrc.jaxrsapi.core.annotation.NonProxy;
import com.suisrc.jaxrsapi.core.annotation.OneTimeProxy;
import com.suisrc.jaxrsapi.core.annotation.RemoteApi;
import com.suisrc.jaxrsapi.core.annotation.Reviser;
import com.suisrc.jaxrsapi.core.annotation.TfDefaultValue;
import com.suisrc.jaxrsapi.core.annotation.Value;
import com.suisrc.jaxrsapi.core.proxy.ProxyBuilder;
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
     * 单接口多服务器模式
     */
    static boolean MULIT_MODE = Boolean.getBoolean(Consts.KEY_REMOTE_API_NULTI_MODE);

    /**
     * 全局偏移量
     * 
     * 防止名字在构建的时候，产生重复
     * 
     * 同时记录程序在运行中构建的数量
     */
    private static volatile int baseOffset = 0;
    
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
        // 获取方法上的注解（这些注解标记在方法的参数上）， 获取参数的顺序为 ThreadValue -> Value -> DefaultValue， 具有左边有限性
        List<AnnotationInstance> annos_m = method.annotations();
        // 分类
        List<AnnotationInstance> value_ms = new ArrayList<>();
        List<AnnotationInstance> defaultValue_ms = new ArrayList<>();
        Map<Short, AnnotationInstance> TfDefaultValue_ms = new HashMap<>();
        List<AnnotationInstance> reviser_ms = new ArrayList<>();
        
        for (AnnotationInstance anno : annos_m) {
            if (anno.target().kind() != Kind.METHOD_PARAMETER) {
                continue;
            }
            String annoName = anno.name().toString();
            if (annoName.equals(Value.class.getName())) {
                value_ms.add(anno);
            } else if (annoName.equals(DefaultValue.class.getName())) {
                defaultValue_ms.add(anno);
            } else if (annoName.equals(TfDefaultValue.class.getName())) {
                short position = anno.target().asMethodParameter().position();
                TfDefaultValue_ms.put(position, anno);
            } else if (annoName.equals(Reviser.class.getName())) {
                reviser_ms.add(anno);
            } else if (annoName.equals(NonProxy.class.getName())) {
                AnnotationValue ave = anno.value();
                Boolean stauts = ave == null ? true : activator.getAdapter(ave.asString());
                if (stauts != null && stauts) {
                    // 不再记性下面的内容构建，相当于禁用了该接口
                    jjc._import(NoSupportException.class);
                    JCall jcall = JTypes.typeOf(NoSupportException.class)._new();
                    jcall.arg(JExprs.str("The interface has been disabled, Config:[" 
                            + (ave == null ? "Permanently Disabled" : ave.asString()) + "]"));
                    body._throw(jcall);
                    return;
                }
            }
        }
        // -----------------------------------------------------------------------------------ZERO parameter Value参数获取部分
        for (AnnotationInstance anno : value_ms) { // Value
            JCall methodExpr = getValueMethodExpr(jjm, anno);
            // 特殊处理
            short position = anno.target().asMethodParameter().position();
            JParamDeclaration param = params.get(position);
            methodExpr.arg(param.type().field("class"));
            //--------
            createParamValueInfo(jjm, body, params, anno, methodExpr);
        }
        // -----------------------------------------------------------------------------------ZERO parameter DefaultValue参数获取部分
        for (AnnotationInstance anno : defaultValue_ms) { // DefaultValue
            short position = anno.target().asMethodParameter().position();
            JCall methodExpr = getDefaultValueMethodExpr(jjm, anno, TfDefaultValue_ms.get(position), parameters.get(position));
            createParamValueInfo(jjm, body, params, anno, methodExpr);
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
                for (AnnotationInstance anno : annos_f) { // SystemValue
                    JCall methodExpr = getValueMethodExpr(jjm, anno);
                    FieldInfo fieldInfo = anno.target().asField();
                    String type = fieldInfo.type().name().toString();
                    jjc._import(type);
                    methodExpr.arg(JTypes.typeNamed(type).field("class"));
                    createFieldValueInfo(jjm, body, params.get(i), anno, methodExpr);
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
                    createFieldValueInfo(jjm, body, params.get(i), anno, methodExpr);
                }
            }
            // -------------------------------------------------------------------------------ZERO 最后的数据修正拦截
            annos_f = classInfo.annotations().get(DotName.createSimple(Reviser.class.getName()));
            if (annos_f != null && !annos_f.isEmpty()) {
                for (AnnotationInstance anno : annos_f) { // InterceptParam
                    JCall methodExpr = getReviserMethodExpr(jjm, anno);
                    createFieldReviserInfo(jjm, body, params.get(i), anno, methodExpr);
                }
            }
        }
        // -------------------------------------------------------------------------------最后的数据修正拦截
        for (AnnotationInstance anno : reviser_ms) { // InterceptParam
            JCall methodExpr = getReviserMethodExpr(jjm, anno);
            short position = anno.target().asMethodParameter().position();
            createParamReviserInfo(jjm, body, params.get(position), anno, methodExpr);
        }
        // -------------------------------------------------------------------------------ZERO 返回值处理
        creatReturnInfo(jjm, body, method, params);
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
        String master = ave == null ? Consts.NONE : ave.asString();
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
        String actname = Consts.FIELD_ACTIVATOR; // 获取activator属性
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
    private void createParamValueInfo(JJMethod jjm, JBlock body, List<JParamDeclaration> params, AnnotationInstance anno, JCall methodExpr) {
        short position = anno.target().asMethodParameter().position();
        JParamDeclaration param = params.get(position);
        // 获取参数
        JAssignableExpr paramVar = JExprs.$v(param);
        // 判断参数是否为空
        JIf jif = body._if(paramVar.eq(JExpr.NULL));
        // 对参数进行赋值
        JVarDeclaration temp = jif.var(0, param.type(), "temp", methodExpr.cast(param.type()));
        JAssignableExpr tempVar = JExprs.$v(temp);
        JIf jif2 = jif._if(tempVar.ne(JExpr.NULL));
        jif2.assign(paramVar, JExprs.$v(temp));
    }

    /**
     * 创建参数属性内容赋值
     * @param body
     * @param param
     * @param anno
     * @param methodExpr
     */
    private void createFieldValueInfo(JJMethod jjm, JBlock body, JParamDeclaration param, AnnotationInstance anno, JCall methodExpr) {
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
        JIf jif = body._if(paramGet.eq(JExpr.NULL));
        JVarDeclaration temp = jif.var(0, type, "temp", methodExpr.cast(type));
        JAssignableExpr tempVar = JExprs.$v(temp);
        paramSet.arg(tempVar);
        JIf jif2 = jif._if(tempVar.ne(JExpr.NULL));
        jif2.add(paramSet);
    }

    /**
     * 构建返回值内容
     * @param method
     * @param anno
     * @return
     */
    private void creatReturnInfo(JJMethod jjm, JBlock body, MethodInfo method, List<JParamDeclaration> params) {
        JCall methodExpr;
        AnnotationInstance anno = method.annotation(DotName.createSimple(LocalProxy.class.getName()));
        if (anno != null) {
            String className = anno.value().asClass().name().toString(); // 代理的类型
            AnnotationValue annoValue = anno.value("master");
            String construct = annoValue == null ? Consts.NONE : annoValue.asString();
            annoValue = anno.value("method");
            String methodName = annoValue == null ? LocalProxy.defaultMethod : annoValue.asString();
            // 赋值
            jjm.getJJClass()._import(className);
            JCall proxy = JTypes.typeNamed(className)._new();
            if (!construct.isEmpty()) {
                proxy.arg(JExprs.$v(construct));
            }
            methodExpr = proxy.call(methodName);
             JCall baseUrl = JExprs.$v(Consts.FIELD_ACTIVATOR).call("getBaseUrl");
             JExpr path = baseUrl;
             if (jjm.getJJClass().getRootPath() != null) {
                 path = path.plus(JExprs.str("/" + jjm.getJJClass().getRootPath()));
             }
             methodExpr.arg(path.plus(JExprs.str(getMethodUri(method))));
//            methodExpr.arg(JExprs.str(getMethodUri(method)));
        } else {
            if (jjm.getJJClass().isOneTimeProxy()) {
                String proxyType = method.declaringClass().name().toString();
                
                jjm.getJJClass()._import(WebTarget.class);
                jjm.getJJClass()._import(ProxyBuilder.class);
                JCall targetCall = JExprs.$v(Consts.FIELD_ACTIVATOR).call("getAdapter").arg(JTypes.typeOf(WebTarget.class).field("class"));
                JVarDeclaration target = body.var(0, WebTarget.class, "target", targetCall.cast(WebTarget.class));
                JCall proxyExpr = JExprs.callStatic(ProxyBuilder.class, "builder");
                proxyExpr.arg(JTypes.typeNamed(proxyType).field("class"));
                proxyExpr.arg(JExprs.$v(target));
                JCall buildExpr = proxyExpr.call("build");
                
                JAssignableExpr proxy = JExprs.$v(body.var(0, proxyType, "proxy", buildExpr));
                methodExpr = proxy.call(method.name());
            } else {
                methodExpr = JExprs.$v(Consts.FIELD_PROXY).call(method.name());
            }
        }
        // 给出方法的参数
        for (JParamDeclaration param : params) {
            methodExpr.arg(JExprs.$v(param));
        }
        anno = method.annotation(DotName.createSimple(Reviser.class.getName()));
        if (anno != null) {
            JCall reviserExpr = getReviserMethodExpr(jjm, anno);
            methodExpr = reviserExpr.arg(methodExpr);
        }
        body._return(methodExpr.cast(method.returnType().name().toString()));
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
        Named named = activator.getClass().getAnnotation(Named.class);
        if (named == null) {
            throw new RuntimeException("Not found 'Named' annotation: " + activator.getClass());
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
        if (MULIT_MODE) { // 单接口多服务器模式
            jjc.setMulitMode(true);
            jjc._import(Named.class);
            anno = jjc.annotate(Named.class);
            jjc.annotateValue(anno, "value", named.value() + Consts.separator + classInfo.name().toString());
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
            JJField pf = jjc.field(JMod.PRIVATE, api, Consts.FIELD_PROXY);
            proxyField = pf.getJVarDeclaration();
            proxyField.blockComment().text("远程代理访问客户端控制器");
        }
        jjc._import(ApiActivator.class);
        JJField af = jjc.field(JMod.PRIVATE, ApiActivator.class, Consts.FIELD_ACTIVATOR);
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
            JCall targetCall = JExprs.$v(activatorField).call("getAdapter").arg(JTypes.typeOf(WebTarget.class).field("class"));
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
        am.annotateValue(anno, "value", named.value());
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
            jjc._import(ScCDI.class);
            JCall cdiExpr = JExprs.callStatic(ScCDI.class, ScCDI.MED_INJECT_WITH_NAMED);
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
    
    // ----------------------------------------------------------------------ZERO 构建入口

    /**
     * 处理递归创建
     * 
     * @param index
     * @param acceptThen
     * @throws Exception
     */
    public static void processIndex(IndexView index, BiConsumer<Class<?>, CtClass> acceptThen, String key) throws Exception {
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
            ClientServiceFactory.createImpl(activator, index, acceptThen, key);
        }
    }

    /**
     * 创建接口实体
     * @param activator
     * @param index
     * @param targetFile
     * @throws Exception
     */
    static void createImpl(ApiActivator activator, IndexView index, JdeJst jj, Consumer<JdeJst> accpetThen, String key) throws Exception {
        int offset = ++baseOffset; // 偏移量递进
        ClientServiceFactory factory = new ClientServiceFactory();
        try {
            for (Class<?> apiClass : activator.getClasses()) {
                ClassInfo info = index.getClassByName(DotName.createSimple(apiClass.getName()));
                // 生成api代理实体
                String name = apiClass.getCanonicalName() + key + offset;
                JJClass jjc = jj.createClass(apiClass, name);
                factory.createImpl(activator, index, jjc, info);
            }
            accpetThen.accept(jj);
        } finally {
            jj.destory();// 清理
        }
    }

    /**
     * 创建接口实体
     * 
     * @param activator
     * @param index
     * @param ctPool
     * @param acceptThen
     * @throws Exception
     */
    static void createImpl(ApiActivator activator, IndexView index, BiConsumer<Class<?>, CtClass> acceptThen, String key) throws Exception {
        createImpl(activator, index, new JdeJst(), jj -> {
            Map<Object, CtClass> ctClasses = jj.writeSource();
            ctClasses.entrySet().forEach(v -> acceptThen.accept((Class<?>)v.getKey(), v.getValue()));
        }, key != null ? key : "_$$jaxrsapi_");
    }

    /**
     * 创建接口实体
     * @param activator
     * @param index
     * @param targetFile
     * @throws Exception
     */
    static void createImpl(ApiActivator activator, IndexView index, String targetFile, String key) throws Exception {
        JdeJst jjst = new JdeJst();
        jjst.setTarget(new File(targetFile));
        jjst.setShowSrc(true); // 需要输出文件
        createImpl(activator, index, jjst, jj -> jj.writeSource4File(), key != null ? key : "_jaxrsapi_");
    }

}
