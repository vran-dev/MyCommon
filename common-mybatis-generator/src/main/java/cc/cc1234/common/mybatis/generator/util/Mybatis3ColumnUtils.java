package cc.cc1234.common.mybatis.generator.util;

import org.mybatis.generator.api.IntrospectedColumn;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

public class Mybatis3ColumnUtils {

    public static String getParameterClause(IntrospectedColumn introspectedColumn) {
        return getParameterClause(introspectedColumn, null);
    }

    public static String getParameterClause(IntrospectedColumn introspectedColumn, String prefix) {
        StringBuilder sb = new StringBuilder();

        sb.append("#{");
        sb.append(introspectedColumn.getJavaProperty(prefix));
        sb.append(",jdbcType=");
        sb.append(introspectedColumn.getJdbcTypeName());

        if (stringHasValue(introspectedColumn.getTypeHandler())) {
            sb.append(",typeHandler=");
            sb.append(introspectedColumn.getTypeHandler());
            sb.append(",javaType=");
            sb.append(introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName());
        }

        sb.append('}');
        return sb.toString();
    }
}
