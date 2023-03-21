package cc.cc1234.common.mybatis.generator.plugin;

import cc.cc1234.common.mybatis.generator.util.Mybatis3ColumnUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InjectJavaTypeToSqlMapPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        // add javaType to ExampleWhereClauseElement
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            if (column.getTypeHandler() != null) {
                XmlElement whereElement = (XmlElement) element.getElements()
                    .stream()
                    .filter(ele -> ele instanceof XmlElement)
                    .findFirst()
                    .orElse(null);
                if (whereElement == null) {
                    return true;
                }
                XmlElement foreachElement = (XmlElement) whereElement.getElements().iterator().next();
                XmlElement ifElement = (XmlElement) foreachElement.getElements().iterator().next();
                XmlElement trimElement = (XmlElement) ifElement.getElements().iterator().next();
                List<XmlElement> otherTypeCriteriaElements = trimElement.getElements()
                    .stream()
                    .map(XmlElement.class::cast)
                    .filter(f -> f.getAttributes()
                        .stream()
                        .filter(attr -> "collection".equals(attr.getName()))
                        .noneMatch(attr -> "criteria.criteria".equals(attr.getValue())))
                    .collect(Collectors.toList());
                for (XmlElement otherTypeCriteriaElement : otherTypeCriteriaElements) {
                    XmlElement chooseElement = (XmlElement) otherTypeCriteriaElement.getElements().iterator().next();
                    for (VisitableElement chooseChild : chooseElement.getElements()) {
                        XmlElement child = XmlElement.class.cast(chooseChild);
                        Optional<Attribute> testAttrOption = child.getAttributes()
                            .stream()
                            .filter(attr -> "test".equals(attr.getName()))
                            .findFirst();
                        if (testAttrOption.isPresent()) {
                            Attribute testAttr = testAttrOption.get();
                            if ("criterion.singleValue".equals(testAttr.getValue())) {
                                String pattern =
                                    "and ${criterion.condition} #{criterion.value,typeHandler=%s,javaType=%s}";
                                String statement = String.format(pattern,
                                    column.getTypeHandler(),
                                    column.getFullyQualifiedJavaType().getFullyQualifiedName());
                                child.getElements().clear();
                                child.getElements().add(new TextElement(statement));
                            } else if ("criterion.betweenValue".equals(testAttr.getValue())) {
                                String pattern =
                                    "and ${criterion.condition} #{criterion.value,typeHandler=%s,javaType=%s}"
                                        + " and #{criterion.secondValue,typeHandler=%s,javaType=%s}";
                                String statement = String.format(pattern,
                                    column.getTypeHandler(),
                                    column.getFullyQualifiedJavaType().getFullyQualifiedName(),
                                    column.getTypeHandler(),
                                    column.getFullyQualifiedJavaType().getFullyQualifiedName());
                                child.getElements().clear();
                                child.getElements().add(new TextElement(statement));
                            } else if ("criterion.listValue".equals(testAttr.getValue())) {
                                String pattern = " #{listItem,typeHandler=%s,javaType=%s}";
                                String statement = String.format(pattern,
                                    column.getTypeHandler(),
                                    column.getFullyQualifiedJavaType().getFullyQualifiedName());
                                child.getElements()
                                    .stream()
                                    .filter(ele -> ele instanceof XmlElement)
                                    .forEach(ele -> {
                                        XmlElement childForeach = (XmlElement) ele;
                                        childForeach.getElements().clear();
                                        childForeach.getElements().add(new TextElement(statement));
                                    });
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element,
                                                                     IntrospectedTable introspectedTable) {
        element.getElements()
            .stream()
            .filter(ele -> ele instanceof XmlElement)
            .map(XmlElement.class::cast)
            .filter(ele -> "set".equals(ele.getName()))
            .forEach(setElement -> {
                setElement.getElements().clear();
                StringBuilder sb = new StringBuilder();
                for (IntrospectedColumn introspectedColumn :
                    ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns())) {
                    sb.setLength(0);
                    sb.append(introspectedColumn.getJavaProperty());
                    sb.append(" != null");
                    XmlElement isNotNullElement = new XmlElement("if");
                    isNotNullElement.addAttribute(new Attribute("test", sb.toString()));
                    setElement.addElement(isNotNullElement);

                    sb.setLength(0);
                    sb.append(MyBatis3FormattingUtilities
                        .getEscapedColumnName(introspectedColumn));
                    sb.append(" = ");
                    sb.append(Mybatis3ColumnUtils.getParameterClause(introspectedColumn));
                    sb.append(',');
                    isNotNullElement.addElement(new TextElement(sb.toString()));
                }
            });
        return true;
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element,
                                                                  IntrospectedTable introspectedTable) {
        element.getElements()
            .stream()
            .filter(ele -> ele instanceof XmlElement)
            .map(XmlElement.class::cast)
            .filter(ele -> "set".equals(ele.getName()))
            .forEach(setElement -> {
                setElement.getElements().clear();
                StringBuilder sb = new StringBuilder();
                for (IntrospectedColumn introspectedColumn :
                    ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getAllColumns())) {
                    sb.setLength(0);
                    sb.append(introspectedColumn.getJavaProperty("record."));
                    sb.append(" != null");
                    XmlElement isNotNullElement = new XmlElement("if");
                    isNotNullElement.addAttribute(new Attribute("test", sb.toString()));
                    setElement.addElement(isNotNullElement);

                    sb.setLength(0);
                    sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
                    sb.append(" = ");
                    sb.append(Mybatis3ColumnUtils.getParameterClause(introspectedColumn, "record."));
                    sb.append(',');
                    isNotNullElement.addElement(new TextElement(sb.toString()));
                }
            });
        return true;
    }

    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element,
                                                                     IntrospectedTable introspectedTable) {
        element.getElements().clear();
        context.getCommentGenerator().addComment(element);

        StringBuilder sb = new StringBuilder();
        sb.append("update ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        element.addElement(new TextElement(sb.toString()));

        sb.setLength(0);
        sb.append("set ");

        Iterator<IntrospectedColumn> iter =
            ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonBLOBColumns()).iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();

            sb.append(MyBatis3FormattingUtilities
                .getAliasedEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(Mybatis3ColumnUtils.getParameterClause(introspectedColumn, "record."));

            if (iter.hasNext()) {
                sb.append(',');
            }

            element.addElement(new TextElement(sb.toString()));

            // set up for the next column
            if (iter.hasNext()) {
                sb.setLength(0);
                OutputUtilities.xmlIndent(sb, 1);
            }
        }

        element.addElement(getUpdateByExampleIncludeElement(introspectedTable));
        return true;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element,
                                                                        IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element,
                                                         IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element,
                                                IntrospectedTable introspectedTable) {
        return true;
    }

    protected XmlElement getUpdateByExampleIncludeElement(IntrospectedTable introspectedTable) {
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "_parameter != null"));

        XmlElement includeElement = new XmlElement("include");
        includeElement.addAttribute(new Attribute("refid",
            introspectedTable.getMyBatis3UpdateByExampleWhereClauseId()));
        ifElement.addElement(includeElement);
        return ifElement;
    }
}
