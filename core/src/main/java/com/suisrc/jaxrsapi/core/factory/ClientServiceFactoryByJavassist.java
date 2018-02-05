package com.suisrc.jaxrsapi.core.factory;

/**
 * 生成执行的代理实体 使用javassist对java代码进行动态生成。
 * 
 * @author Y13
 * 
 * @since 170801 弃用，决定使用jdeparser+javassist结合的方式，便于代码的维护和管理, 所以暂时舍弃该类
 */
@Deprecated
public class ClientServiceFactoryByJavassist {
//
//    /**
//     * 单接口多服务器模式
//     */
//    private static boolean MULIT_MODE = Boolean.valueOf(System.getProperty(Consts.KEY_REMOTE_API_NULTI_MODE, "false"));
//
//    /**
//     * 全局偏移量
//     */
//    private static volatile int baseOffset = 0;
//
//    /**
//     * 结果
//     */
//    private static final String RESULT = "result";
//
//    /**
//     * 代理生成模版
//     */
//    // { proxy = ProxyBuilder.builder( ApiType.class,
//    // (WebTarget)activator.getAdapter(WebTarget.class) ).build(); }
//    private static final String InitMethodModule = "{ " + Consts.FIELD_PROXY + " = " + ProxyBuilder.class.getCanonicalName()
//            + ".builder({ApiType}.class, (" + WebTarget.class.getCanonicalName() + ")" + Consts.FIELD_ACTIVATOR + ".getAdapter("
//            + WebTarget.class.getCanonicalName() + ".class)).build(); }";
//    /**
//     * 系统常数模版
//     */
//    private static final String SystemParamModule =
//            "if( ${Param} == null ) { ${Param} = ({ParamType}){Master}.{MethodName}(\"{Value}\"); } ";
//    private static final String SystemFieldModule =
//            "if( ${Param}.{GetField}() == null ) { ${Param}.{SetField}(({FieldType}){Master}.{MethodName}(\"{Value}\")); } ";
//
//    private static final String DefaultParamModule =
//            "if( ${Param} == null ) { ${Param} = ({ParamType}){Master}.{MethodName}({ParamType}.class, \"{Value}\"); } ";
//    private static final String DefaultFieldModule =
//            "if( ${Param}.{GetField}() == null ) { ${Param}.{SetField}(({FieldType}){Master}.{MethodName}({FieldType}.class, \"{Value}\")); } ";
//
//    private static final String InterceptorParamModule =
//            "${Param} = ({ParamType})new {Value}({Master}).{MethodName}(${Param}); ";
//    private static final String InterceptorFieldModule =
//            "${Param}.{SetField}(({FieldType})new {Value}({Master}).{MethodName}(${Param})); ";
//
//    private static final String InterceptorResultModule = InterceptorParamModule.replace("$", ""); // 结果拦截内容和参数拦截内容相同
//    // Type result = proxy.Method(params); Interceptor return result;
//    private static final String ReturnModule =
//            "{ReturnType} " + RESULT + " = {Proxy}.{Method}({Params}); {Interceptor}return " + RESULT + ";";
//
//    /**
//     * 创建接口实现
//     * 
//     * @param activator
//     * @param index
//     * @param ctPool
//     * @param implName
//     * @param classInfo
//     * @return
//     * @throws Exception
//     */
//    private static CtClass createImpl(ApiActivator activator, IndexView index, ClassPool ctPool, String implName,
//            ClassInfo classInfo) throws Exception {
//        Named named = activator.getClass().getAnnotation(Named.class);
//        if (named == null) {
//            throw new RuntimeException("Not found Named Annotation : " + activator.getClass());
//        }
//        CtClass ctClass = ctPool.makeClass(implName);
//        crateBaseInfo(ctPool, classInfo, named, ctClass);
//        for (MethodInfo methodInfo : classInfo.methods()) {
//            if (isProxyMethod(methodInfo)) {
//                createMethod(index, ctPool, ctClass, methodInfo);
//            }
//        }
//        debugCtClass(ctClass);
//        return ctClass;
//    }
//
//    /**
//     * 是否需要进行代理 判断是否需要执行代理
//     * 
//     * @param info
//     * @return
//     */
//    private static boolean isProxyMethod(MethodInfo methodInfo) {
//        if (methodInfo.name().equals("<init>") || methodInfo.name().startsWith("as")) {
//            // || methodInfo.hasAnnotation(DotName.createSimple(NonProxy.class.getCanonicalName())))
//            // {
//            return false; // 一些初始化和构造方法
//        }
//        return methodInfo.hasAnnotation(DotName.createSimple(GET.class.getCanonicalName()))
//                || methodInfo.hasAnnotation(DotName.createSimple(POST.class.getCanonicalName()))
//                || methodInfo.hasAnnotation(DotName.createSimple(PUT.class.getCanonicalName()))
//                || methodInfo.hasAnnotation(DotName.createSimple(DELETE.class.getCanonicalName()));
//    }
//
//    /**
//     * 构建代理的方法
//     * 
//     * @param index
//     * @param ctClass
//     * @param method
//     * @throws CannotCompileException
//     * @throws NotFoundException
//     * @throws ClassNotFoundException
//     */
//    private static void createMethod(IndexView index, ClassPool ctPool, CtClass ctClass, MethodInfo method)
//            throws CannotCompileException, ClassNotFoundException, NotFoundException {
//
//        List<Type> parameters = method.parameters(); // 获取参数的
//        StringBuilder methodContent = new StringBuilder("{ ");
//        // -----------------------------------------------------------------------------------//
//        List<AnnotationInstance> annos_m = method.annotations();
//        for (AnnotationInstance anno : annos_m) { // SystemValue
//            if (anno.name().toString().equals(Value.class.getCanonicalName())) {
//                methodContent.append(
//                        createParamModule(SystemParamModule, anno, null, parameters, Consts.FIELD_ACTIVATOR, "getAdapter"));
//            }
//        }
//        for (AnnotationInstance anno : annos_m) { // ThreadValue
//            if (anno.name().toString().equals(ThreadValue.class.getCanonicalName())) {
//                AnnotationValue ave = anno.value("clazz");
//                String actname = ave != null ? ave.asClass().toString() : Global.class.getCanonicalName();
//                ave = anno.value("method");
//                String metname = ave != null ? ave.asString() : ThreadValue.defaultMethod;
//                methodContent.append(createParamModule(SystemParamModule, anno, null, parameters, actname, metname));
//            }
//        }
//        for (AnnotationInstance anno : annos_m) { // DefaultValue
//            if (anno.name().toString().equals(DefaultValue.class.getCanonicalName())) {
//                methodContent.append(createParamModule(DefaultParamModule, anno, null, parameters,
//                        TransformUtils.class.getCanonicalName(), TransformUtils.METHOD));
//            }
//        }
//
//        // 参数
//        StringBuilder paramsContent = new StringBuilder();
//        CtClass[] ctParameters = new CtClass[parameters.size()];
//        for (int i = 0; i < parameters.size(); i++) {
//            paramsContent.append('$').append(i + 1).append(',');
//            Type paramType = parameters.get(i);
//            ctParameters[i] = JaxrsapiUtils.getCtClass(ctPool, paramType.name().toString());
//
//            ClassInfo classInfo = index.getClassByName(paramType.name());
//            if (classInfo == null) {
//                continue;
//            }
//            List<AnnotationInstance> annos_f = classInfo.annotations().get(DotName.createSimple(Value.class.getName()));
//            if (annos_f != null && !annos_f.isEmpty()) {
//                for (AnnotationInstance anno : annos_f) { // SystemValue
//                    methodContent
//                            .append(createFieldModule(SystemFieldModule, anno, null, i, Consts.FIELD_ACTIVATOR, "getAdapter"));
//                }
//            }
//            annos_f = classInfo.annotations().get(DotName.createSimple(ThreadValue.class.getName()));
//            if (annos_f != null && !annos_f.isEmpty()) {
//                for (AnnotationInstance anno : annos_f) { // ThreadValue
//                    AnnotationValue ave = anno.value("clazz");
//                    String actname = ave != null ? ave.asClass().toString() : Global.class.getCanonicalName();
//                    ave = anno.value("method");
//                    String metname = ave != null ? ave.asString() : ThreadValue.defaultMethod;
//                    methodContent.append(createFieldModule(SystemFieldModule, anno, null, i, actname, metname));
//                }
//            }
//            annos_f = classInfo.annotations().get(DotName.createSimple(DefaultValue.class.getName()));
//            if (annos_f != null && !annos_f.isEmpty()) {
//                for (AnnotationInstance anno : annos_f) { // DefaultValue
//                    methodContent.append(createFieldModule(DefaultFieldModule, anno, null, i,
//                            TransformUtils.class.getCanonicalName(), "transform"));
//                }
//            }
//            // ----------------------------------最后的数据修正拦截---------------------------------------------//
//            annos_f = classInfo.annotations().get(DotName.createSimple(Reviser.class.getName()));
//            if (annos_f != null && !annos_f.isEmpty()) {
//                for (AnnotationInstance anno : annos_f) { // InterceptParam
//                    String value = anno.value().asClass().name().toString();
//                    AnnotationValue annoValue = anno.value("master");
//                    String master = annoValue == null ? Consts.NONE : annoValue.asString();
//                    methodContent
//                            .append(createFieldModule(InterceptorFieldModule, anno, value, i, master, ReviseHandler.METHOD));
//                }
//            }
//        }
//        // ----------------------------------最后的数据修正拦截---------------------------------------------//
//        for (AnnotationInstance anno : annos_m) { // InterceptParam
//            if (anno.name().toString().equals(Reviser.class.getCanonicalName())) {
//                String value = anno.value().asClass().name().toString();
//                AnnotationValue annoValue = anno.value("master");
//                String master = annoValue == null ? Consts.NONE : annoValue.asString();
//                methodContent.append(
//                        createParamModule(InterceptorParamModule, anno, value, parameters, master, ReviseHandler.METHOD));
//            }
//        }
//        // -----------------------------------------------------------------------------------------------------//
//        if (paramsContent.length() > 0) {
//            paramsContent.setLength(paramsContent.length() - 1);
//        }
//
//        methodContent.append(creatReturnContent(method, paramsContent.toString())).append("}");
//        // 返回值
//        CtClass returnType = JaxrsapiUtils.getCtClass(ctPool, method.returnType().name().toString());
//        // 异常
//        List<Type> exceptions = method.exceptions();
//        CtClass[] ctExceptions = new CtClass[exceptions.size()];
//        for (int i = 0; i < exceptions.size(); i++) {
//            ctExceptions[i] = JaxrsapiUtils.getCtClass(ctPool, exceptions.get(i).name().toString());
//        }
//        CtMethod ctMethod =
//                CtNewMethod.make(returnType, method.name(), ctParameters, ctExceptions, methodContent.toString(), ctClass);
//        ctClass.addMethod(ctMethod);
//    }
//
//    /**
//     * 创建返回内容 "{ReturnType} " + RESULT + " = {Proxy}.{Method}({Params}); {Interceptor}return " +
//     * RESULT + ";"; "${Param} = ({ParamType})new {Value}({Master}).{MethodName}(${Param}); ";
//     * 
//     * @param method
//     * @param anno
//     * @return
//     */
//    private static String creatReturnContent(MethodInfo method, String params) {
//        String proxy = Consts.FIELD_PROXY; // 代理对象
//        String methodName = method.name(); // 代理方法
//        AnnotationInstance anno = method.annotation(DotName.createSimple(LocalProxy.class.getCanonicalName()));
//        if (anno != null) {
//            params = "\"" + getMethodUri(method) + "\"," + params; // 参数增加URI
//
//            String className = anno.value().asClass().name().toString(); // 代理的类型
//            AnnotationValue annoValue = anno.value("master");
//            String construct = annoValue == null ? Consts.NONE : annoValue.asString();
//            annoValue = anno.value("method");
//            methodName = annoValue == null ? LocalProxy.defaultMethod : annoValue.asString();
//            proxy = "new " + className + "(" + construct + ")";
//        }
//        String returnType = method.returnType().name().toString(); // 返回值类型
//        String interceptor = "";
//        anno = method.annotation(DotName.createSimple(Reviser.class.getCanonicalName()));
//        if (anno != null) {
//            String className = anno.value().asClass().name().toString();
//            AnnotationValue annoValue = anno.value("master");
//            String construct = annoValue == null ? Consts.NONE : annoValue.asString();
//            interceptor =
//                    createResultModule(InterceptorResultModule, RESULT, returnType, className, construct, ReviseHandler.METHOD);
//        }
//        if (interceptor.isEmpty()) { // 简化代码生成
//            return "return " + proxy + "." + methodName + "(" + params + ");";
//        }
//        return ReturnModule.replace("{ReturnType}", returnType).replace("{Proxy}", proxy).replace("{Method}", methodName)
//                .replace("{Params}", params).replace("{Interceptor}", interceptor);
//    }
//
//    /**
//     * 获取uri
//     */
//    private static String getMethodUri(MethodInfo method) {
//        String path = "";
//        DotName pathAnnoName = DotName.createSimple(Path.class.getCanonicalName());
//        for (AnnotationInstance ai : method.declaringClass().classAnnotations()) {
//            if (ai.name().equals(pathAnnoName)) {
//                path += "/" + ai.value().asString();
//            }
//        }
//        AnnotationInstance ai = method.annotation(pathAnnoName);
//        if (ai != null) {
//            path += "/" + ai.value().asString();
//        }
//        if (!path.isEmpty()) {
//            path = path.replaceAll("/{2,}", "/");
//            if (path.charAt(path.length() - 1) == '/') {
//                path = path.substring(0, path.length() - 1);
//            }
//        }
//        return path;
//    }
//
//    /**
//     * 
//     * @param methodContent
//     * @param anno
//     * @param position
//     */
//    private static String createFieldModule(String module, AnnotationInstance anno, String annoValue, int position,
//            String master, String methodName) {
//        FieldInfo fieldInfo = anno.target().asField();
//        String name = fieldInfo.name();
//        name = name.substring(0, 1).toUpperCase() + name.substring(1);
//        String getMethod = "get" + name;
//        String setMethod = "set" + name;
//        if (fieldInfo.declaringClass().method(getMethod) == null) {
//            name = "is" + name;
//        }
//        return module.replace("{Param}", position + 1 + "").replace("{FieldType}", fieldInfo.type().toString())
//                .replace("{Value}", annoValue != null ? annoValue : anno.value().asString()).replace("{Master}", master)
//                .replace("{MethodName}", methodName).replace("{GetField}", getMethod).replace("{SetField}", setMethod);
//    }
//
//    /**
//     * 
//     * @param methodContent
//     * @param anno
//     * @param parameters
//     */
//    private static String createParamModule(String module, AnnotationInstance anno, String annoValue, List<Type> parameters,
//            String master, String methodName) {
//        short position = anno.target().asMethodParameter().position();
//        return module.replace("{Param}", position + 1 + "").replace("{ParamType}", parameters.get(position).name().toString())
//                .replace("{Value}", annoValue != null ? annoValue : anno.value().asString()).replace("{Master}", master)
//                .replace("{MethodName}", methodName);
//    }
//
//    /**
//     * 
//     * @param methodContent
//     * @param anno
//     * @param parameters
//     */
//    private static String createResultModule(String module, String result, String type, String clazz, String master,
//            String method) {
//        return module.replace("{Param}", result).replace("{ParamType}", type).replace("{Value}", clazz)
//                .replace("{Master}", master).replace("{MethodName}", method);
//    }
//
//    /**
//     * 构建基础信息
//     * 
//     * @param ctPool
//     * @param classInfo
//     * @param named
//     * @param ctClass
//     * @throws NotFoundException
//     * @throws ClassNotFoundException
//     * @throws CannotCompileException
//     */
//    private static void crateBaseInfo(ClassPool ctPool, ClassInfo classInfo, Named named, CtClass ctClass)
//            throws NotFoundException, ClassNotFoundException, CannotCompileException {
//        // 代理的接口
//        CtClass ctApiClass = JaxrsapiUtils.getCtClass(ctPool, classInfo.name().toString());
//        // 增加注解
//        ClassFile ctFile = ctClass.getClassFile();
//        ConstPool constPool = ctFile.getConstPool();
//        AnnotationsAttribute attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
//        Annotation annotation = new Annotation(ApplicationScoped.class.getCanonicalName(), constPool);
//        attribute.addAnnotation(annotation);
//        if (MULIT_MODE) { // 单接口多服务器模式
//            annotation = new Annotation(Named.class.getCanonicalName(), constPool);
//            annotation.addMemberValue("value",
//                    new StringMemberValue(named.value() + Consts.separator + ctApiClass.getSimpleName(), constPool));
//        }
//        attribute.addAnnotation(annotation);
//        ctFile.addAttribute(attribute);
//
//        // 集成
//        ctClass.addInterface(ctApiClass); // 继承与通信API
//        ctClass.addInterface(JaxrsapiUtils.getCtClass(ctPool, ServiceClient.class)); // 继承与ServiceClient
//
//        /*
//         * private UserRest proxy;
//         */
//        CtField ctProxyField = new CtField(ctApiClass, Consts.FIELD_PROXY, ctClass); // 执行代理
//        ctProxyField.setModifiers(Modifier.PRIVATE);
//        ctClass.addField(ctProxyField);
//
//        /*
//         * @Inject @Named("xxxx") private ApiActivator activator;
//         */
//        CtClass ctApiActivatorClass = JaxrsapiUtils.getCtClass(ctPool, ApiActivator.class);
//        CtField ctApiActivatorField = new CtField(ctApiActivatorClass, Consts.FIELD_ACTIVATOR, ctClass); // 执行代理
//        // 增加注解
//        attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
//        annotation = new Annotation(Inject.class.getCanonicalName(), constPool);
//        attribute.addAnnotation(annotation);
//        annotation = new Annotation(Named.class.getCanonicalName(), constPool);
//        annotation.addMemberValue("value", new StringMemberValue(named.value(), constPool));
//        attribute.addAnnotation(annotation);
//        ctApiActivatorField.getFieldInfo().addAttribute(attribute);
//        ctApiActivatorField.setModifiers(Modifier.PRIVATE);
//        ctClass.addField(ctApiActivatorField);
//        ctClass.addMethod(CtNewMethod.getter("getActivator", ctApiActivatorField));
//        ctClass.addMethod(CtNewMethod.setter("setActivator", ctApiActivatorField));
//
//        String methodContent = InitMethodModule.replace("{ApiType}", ctApiClass.getName());
//        CtMethod ctInitializedMethod = CtNewMethod.make(CtClass.voidType, "initialized", null, null, methodContent, ctClass);
//        // 增加注解
//        attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
//        annotation = new Annotation(PostConstruct.class.getCanonicalName(), constPool);
//        attribute.addAnnotation(annotation);
//        ctInitializedMethod.getMethodInfo().addAttribute(attribute);
//        ctClass.addMethod(ctInitializedMethod);
//    }
//    // ------------------------------------------------------------------------------------------------------------//
//
//    /**
//     * 处理递归创建
//     * 
//     * @param index
//     * @param acceptThen
//     * @throws Exception
//     */
//    public static void processIndex(IndexView index, BiConsumer<Class<?>, CtClass> acceptThen) throws Exception {
//        ClassPool ctPool = ClassPool.getDefault();
//        List<ClassInfo> activatorClasses =
//                index.getKnownDirectImplementors((DotName.createSimple(ApiActivator.class.getName())));
//        Set<Class<?>> activatorSet = new HashSet<>();
//        Set<Class<?>> subclasses = new HashSet<>(); // 用于判断是否为其他实体的继承
//        for (ClassInfo activatorClass : activatorClasses) {
//            try {// 加载对象
//                Class<?> classActivator = (Class<?>) Class.forName(activatorClass.name().toString());
//                activatorSet.add(classActivator);
//                subclasses.add(classActivator.getSuperclass());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        for (Class<?> classActivator : activatorSet) {
//            if (subclasses.contains(classActivator)) {
//                continue;
//            } // 有激活器继承该激活器，舍弃不予加载
//            if (classActivator.isInterface() || Modifier.isAbstract(classActivator.getModifiers())) {
//                continue;
//            }
//            ApiActivator activator = (ApiActivator) classActivator.newInstance();
//            createImpl(activator, index, ctPool, acceptThen);
//        }
//    }
//
//    /**
//     * 创建接口实体
//     * 
//     * @param activator
//     * @param index
//     * @param ctPool
//     * @param acceptThen
//     * @throws Exception
//     */
//    static void createImpl(ApiActivator activator, IndexView index, ClassPool ctPool, BiConsumer<Class<?>, CtClass> acceptThen)
//            throws Exception {
//        int offset = ++baseOffset; // 偏移量递进
//        for (Class<?> apiClass : activator.getClasses()) {
//            ClassInfo info = index.getClassByName(DotName.createSimple(apiClass.getName()));
//            // 生成api代理实体
//            String name = apiClass.getCanonicalName() + "_$$jaxrsapi_" + offset;
//            CtClass ctClass = ClientServiceFactoryByJavassist.createImpl(activator, index, ctPool, name, info);
//            try {
//                acceptThen.accept(apiClass, ctClass);
//            } finally {
//                ctClass.freeze(); // 释放
//            }
//        }
//    }
//
//    // ----------------------------------------------TEST--------------------------------------------------------//
//    private static void debugCtClass(CtClass ctClass) {
//        try {
//            ctClass.writeFile("D:/classes");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
