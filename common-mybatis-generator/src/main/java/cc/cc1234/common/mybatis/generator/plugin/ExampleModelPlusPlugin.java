package cc.cc1234.common.mybatis.generator.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.Collections;
import java.util.List;

public class ExampleModelPlusPlugin extends PluginAdapter {

    private static final String DISABLE_ORDER_BY_PROP = "example.order-by.disabled";

    private static final String DISABLE_STATIC_FACTORY_PROP = "example.static-factory.disabled";

    private static final String DISABLE_EXAMPLE_PROP = "criteria.example.disabled";

    private static final String DISABLE_PAGE_PROP = "example.page.disabled";

    private List<String> warnings;

    @Override
    public boolean validate(List<String> warnings) {
        this.warnings = warnings;
        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
                                                                     IntrospectedTable introspectedTable) {
        //     <if test="orderByClause != null">
        //      order by ${orderByClause}
        //    </if>
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "pageCriteria != null"));
        ifElement.addElement(new TextElement("limit #{pageCriteria.limit} offset #{pageCriteria.offset}"));
        element.addElement(ifElement);
        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element,
                                                                  IntrospectedTable introspectedTable) {
        return super.sqlMapSelectByExampleWithBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if ("false".equals(properties.getProperty(DISABLE_EXAMPLE_PROP, "false"))) {
            addExampleMethodInCriteria(topLevelClass);
        }

        if ("false".equals(properties.getProperty(DISABLE_STATIC_FACTORY_PROP, "false"))) {
            addStaticFactoryInExample(topLevelClass);
        }

        if ("false".equals(properties.getProperty(DISABLE_ORDER_BY_PROP, "false"))) {
            addOrderByMethodInExample(topLevelClass);
            addOrderByFieldInExample(topLevelClass);
            updateClearMethodInExample(topLevelClass);
            addOrderByCriteriaClassInExample(topLevelClass, introspectedTable);
            updateGetOrderByClause(topLevelClass);
            updateSetOrderByClause(topLevelClass);
        }

        if ("false".equals(properties.getProperty(DISABLE_PAGE_PROP, "false"))) {
            addInnerPageClassToExample(topLevelClass);
            addPageCriteriaBeanToExample(topLevelClass);
            addLimitAndOffsetMethodToExample(topLevelClass);
        }
        return true;
    }

    private void addStaticFactoryInExample(TopLevelClass exampleModel) {
        Method method = new Method("create");
        method.setStatic(true);
        method.setReturnType(exampleModel.getType());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addBodyLine("return new " + exampleModel.getType().getShortName() + "();");
        exampleModel.addImportedType(exampleModel.getType());
        exampleModel.addMethod(method);
    }

    private void addExampleMethodInCriteria(TopLevelClass exampleModel) {
        exampleModel.getMethods()
                .stream()
                .filter(method -> method.getName().equals("createCriteriaInternal"))
                .findFirst()
                .ifPresent(method -> {
                    method.getBodyLines().clear();
                    method.addBodyLine("Criteria criteria = new Criteria(this);");
                    method.addBodyLine("return criteria;");
                });

        exampleModel.getInnerClasses()
                .stream()
                .filter(inner -> inner.getSuperClass().isPresent() && inner.isStatic())
                .findFirst()
                .ifPresent(inner -> {
                    FullyQualifiedJavaType exampleType = exampleModel.getType();

                    Field exampleField = new Field("example", exampleType);
                    exampleField.setFinal(true);
                    exampleField.setVisibility(JavaVisibility.PRIVATE);
                    inner.addField(exampleField);

                    inner.getMethods()
                            .stream()
                            .filter(Method::isConstructor)
                            .findFirst()
                            .ifPresent(constructor -> {
                                Parameter exampleParameter = new Parameter(exampleType, "example");
                                constructor.addParameter(exampleParameter);
                                constructor.addBodyLines(Collections.singletonList("this.example = example;"));
                            });

                    Method method = new Method("example");
                    method.setStatic(false);
                    method.setReturnType(exampleModel.getType());
                    method.setVisibility(JavaVisibility.PUBLIC);
                    method.addBodyLine("return this.example;");
                    exampleModel.addImportedType(exampleModel.getType());
                    inner.addMethod(method);
                });
    }

    /**
     * <pre>
     *     public OrderByCriteria orderBy() {
     *         return new OrderByCriteria(this);
     *     }
     * </pre>
     */
    private void addOrderByMethodInExample(TopLevelClass topLevelClass) {
        Method method = new Method("orderBy");
        method.setStatic(false);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("OrderByCriteria"));
        method.addBodyLine("return new OrderByCriteria(this);");
        topLevelClass.addMethod(method);
    }

    private void updateClearMethodInExample(TopLevelClass topLevelClass) {
        topLevelClass.getMethods()
                .stream()
                .filter(method -> method.getName().equals("clear"))
                .findFirst()
                .ifPresent(method -> {
                    method.addBodyLine("orderBy.clear();");
                });
    }

    private void updateSetOrderByClause(TopLevelClass topLevelClass) {
        topLevelClass.getMethods()
                .stream()
                .filter(method -> method.getName().equals("setOrderByClause"))
                .findFirst()
                .ifPresent(method -> {
                    method.getBodyLines().clear();
                    method.addBodyLine("this.orderBy.add(orderByClause);");
                });
    }

    private void updateGetOrderByClause(TopLevelClass topLevelClass) {
        topLevelClass.getMethods()
                .stream()
                .filter(method -> method.getName().equals("getOrderByClause"))
                .findFirst()
                .ifPresent(method -> {
                    method.getBodyLines().clear();
                    method.addBodyLine("if (orderBy.isEmpty()) {");
                    method.addBodyLine("    return this.orderByClause;");
                    method.addBodyLine("}");
                    method.addBodyLine("else {");
                    method.addBodyLine("    return String.join(\",\", orderBy);");
                    method.addBodyLine("}");
                });
    }

    private void addOrderByFieldInExample(TopLevelClass topLevelClass) {
        FullyQualifiedJavaType type = new FullyQualifiedJavaType("java.util.List");
        type.addTypeArgument(new FullyQualifiedJavaType("String"));
        Field orderByField = new Field("orderBy", type);
        orderByField.setInitializationString("new ArrayList<>()");
        orderByField.setVisibility(JavaVisibility.PRIVATE);
        orderByField.setFinal(true);
        topLevelClass.addField(orderByField);
    }

    private void addOrderByCriteriaClassInExample(TopLevelClass topLevelClass, IntrospectedTable table) {
        TopLevelClass orderByCriteria = new TopLevelClass("OrderByCriteria");
        orderByCriteria.setVisibility(JavaVisibility.PUBLIC);
        orderByCriteria.setStatic(true);

        Field exampleField = new Field("example", topLevelClass.getType());
        exampleField.setVisibility(JavaVisibility.PRIVATE);
        exampleField.setFinal(true);
        orderByCriteria.addField(exampleField);

        Method constructor = new Method("OrderByCriteria");
        constructor.setVisibility(JavaVisibility.PROTECTED);
        constructor.setConstructor(true);
        constructor.addBodyLine("this.example = example;");
        constructor.addParameter(new Parameter(topLevelClass.getType(), "example"));
        orderByCriteria.addMethod(constructor);

        Method exampleMethod = new Method("example");
        exampleMethod.setVisibility(JavaVisibility.PUBLIC);
        exampleMethod.setReturnType(topLevelClass.getType());
        exampleMethod.addBodyLine("return this.example;");
        orderByCriteria.addMethod(exampleMethod);

        table.getAllColumns().forEach(column -> {
            orderByCriteria.addMethod(columnOrderMethod(column, "Asc"));
            orderByCriteria.addMethod(columnOrderMethod(column, "Desc"));
        });

        topLevelClass.addInnerClass(orderByCriteria);
    }

    private Method columnOrderMethod(IntrospectedColumn column, String sort) {
        String columnName = column.getActualColumnName();
        String propName = column.getJavaProperty();
        Method method = new Method(propName + sort);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("OrderByCriteria"));
        method.addBodyLine("this.example.orderBy.add(\"" + columnName + " " + sort.toUpperCase() + "\");");
        method.addBodyLine("return this;");
        return method;
    }

    private void addInnerPageClassToExample(TopLevelClass topLevelClass) {
        TopLevelClass pageCriteria = new TopLevelClass("PageCriteria");
        pageCriteria.setStatic(true);
        pageCriteria.setVisibility(JavaVisibility.PUBLIC);

        Field limitField = new Field("limit", new FullyQualifiedJavaType("java.lang.Long"));
        limitField.setVisibility(JavaVisibility.PRIVATE);
        pageCriteria.addField(limitField);

        Field offsetField = new Field("offset", new FullyQualifiedJavaType("java.lang.Long"));
        offsetField.setVisibility(JavaVisibility.PRIVATE);
        pageCriteria.addField(offsetField);

        pageCriteria.addMethod(generateGetter("getLimit", "limit", "java.lang.Long"));
        pageCriteria.addMethod(generateSetter("setLimit", "limit", "java.lang.Long"));
        pageCriteria.addMethod(generateGetter("getOffset", "offset", "java.lang.Long"));
        pageCriteria.addMethod(generateSetter("setOffset", "offset", "java.lang.Long"));
        topLevelClass.addInnerClass(pageCriteria);
    }

    private void addPageCriteriaBeanToExample(TopLevelClass topLevelClass) {
        // private PageCriteria pageCriteria
        Field pageField =
                new Field("pageCriteria", new FullyQualifiedJavaType("PageCriteria"));
        pageField.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(pageField);

        // public PageCriteria getPageCriteria()
        topLevelClass.addMethod(generateGetter("getPageCriteria", "pageCriteria", "PageCriteria"));
        // public PageCriteria setPageCriteria()
        topLevelClass.addMethod(generateSetter("setPageCriteria", "pageCriteria", "PageCriteria"));
    }

    private void addLimitAndOffsetMethodToExample(TopLevelClass topLevelClass) {
        // public XxxExample offset(Long offset)
        Method offset = new Method("offset");
        offset.setVisibility(JavaVisibility.PUBLIC);
        offset.setReturnType(topLevelClass.getType());
        offset.addParameter(
                new Parameter(new FullyQualifiedJavaType("java.lang.Long"), "offset"));
        offset.addBodyLine("if (this.pageCriteria == null) {");
        offset.addBodyLine("this.pageCriteria = new PageCriteria();");
        offset.addBodyLine("}");
        offset.addBodyLine("this.pageCriteria.setOffset(offset);");
        offset.addBodyLine("return this;");
        topLevelClass.addMethod(offset);

        // public XxxExample limit(Long limit)
        Method limit = new Method("limit");
        limit.setReturnType(topLevelClass.getType());
        limit.setVisibility(JavaVisibility.PUBLIC);
        limit.addParameter(
                new Parameter(new FullyQualifiedJavaType("java.lang.Long"), "limit"));
        limit.addBodyLine("if (this.pageCriteria == null) {");
        limit.addBodyLine("this.pageCriteria = new PageCriteria();");
        limit.addBodyLine("}");
        limit.addBodyLine("this.pageCriteria.setLimit(limit);");
        limit.addBodyLine("return this;");
        topLevelClass.addMethod(limit);
    }

    private Method generateGetter(String name, String propertyName, String returnType) {
        Method method = new Method(name);
        method.setReturnType(new FullyQualifiedJavaType(returnType));
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addBodyLine("return this." + propertyName + ";");
        return method;
    }

    private Method generateSetter(String name, String propertyName, String parameterType) {
        Parameter parameter = new Parameter(new FullyQualifiedJavaType(parameterType), propertyName);
        Method method = new Method(name);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(parameter);
        method.addBodyLine("this." + propertyName + " = " + propertyName + ";");
        return method;
    }
}