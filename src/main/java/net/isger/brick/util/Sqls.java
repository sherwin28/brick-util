package net.isger.brick.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import net.isger.brick.util.anno.Alias;

/**
 * 关系型数据库结构化查询工具
 * 
 * @author issing
 * 
 */
public class Sqls {

    private static Map<Class<?>, Properties> sqls;

    static {
        sqls = new HashMap<Class<?>, Properties>();
    }

    public static Object[] translate(ResultSet resultSet) {
        List<Object[]> result = new ArrayList<Object[]>();
        String[] columns = null;
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int count = metaData.getColumnCount();
            columns = new String[count];
            for (int i = 0; i < count;) {
                columns[i] = metaData.getColumnLabel(++i).toLowerCase();
            }

            Object[] info = null;
            while (resultSet.next()) {
                info = new Object[count];
                for (int i = 0; i < count;) {
                    info[i] = resultSet.getObject(++i);
                }
                result.add(info);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new Object[] { columns, result.toArray(new Object[][] {}) };
    }

    public static int modify(Class<?> clazz, String id, Object[] values,
            Connection conn, Object... args) {
        return modify(getSQL(clazz, id, args), values, conn);
    }

    public static int[] modify(Class<?> clazz, String id, Object[][] values,
            Connection conn, Object... args) {
        return modify(getSQL(clazz, id, args), values, conn);
    }

    public static Object modify(SQLEntry entry, Connection conn)
            throws RuntimeException {
        String sql = entry.getSQL();
        Object values = entry.getValues();
        if (values instanceof Object[][]) {
            return modify(sql, (Object[][]) values, conn);
        }
        return modify(sql, (Object[]) values, conn);
    }

    public static int[] modify(String sql, Object[][] values, Connection conn) {
        PreparedStatement stat = getStatement(sql, values, conn);
        try {
            return stat.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        } finally {
            close(stat);
        }
    }

    public static int modify(String sql, Object[] values, Connection conn)
            throws RuntimeException {
        PreparedStatement stat = getStatement(sql, values, conn);
        try {
            return stat.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(stat);
        }
    }

    public static Object[] query(Class<?> clazz, String id, Object[] values,
            Connection conn, Object... args) {
        return query(getSQL(clazz, id, args), values, conn);
    }

    public static Object[] query(SQLEntry entry, Connection conn)
            throws RuntimeException {
        return query(entry.getSQL(), entry.getValues(), conn);
    }

    public static Object[] query(String sql, Object[] values, Connection conn) {
        PreparedStatement stat = getStatement(sql, values, conn);
        ResultSet resultSet = null;
        try {
            resultSet = stat.executeQuery();
            return translate(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(resultSet);
            close(stat);
        }
    }

    private static PreparedStatement getStatement(String sql, Object[] values,
            Connection conn) throws RuntimeException {
        PreparedStatement stat = null;
        try {
            stat = conn.prepareStatement(sql);
            int amount = 0;
            if (values != null) {
                for (Object value : values) {
                    if (value instanceof Date) {
                        stat.setObject(++amount,
                                new Timestamp(((Date) value).getTime()));
                    } else {
                        stat.setObject(++amount, value);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return stat;
    }

    private static PreparedStatement getStatement(String sql,
            Object[][] values, Connection conn) throws RuntimeException {
        PreparedStatement stat = null;
        try {
            stat = conn.prepareStatement(sql);
            if (values != null) {
                for (Object[] batch : values) {
                    int amount = 0;
                    for (Object value : batch) {
                        if (value instanceof Date) {
                            stat.setObject(++amount, new Timestamp(
                                    ((Date) value).getTime()));
                        } else {
                            stat.setObject(++amount, value);
                        }
                    }
                    stat.addBatch();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        return stat;
    }

    public static void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
            }
        }
    }

    public static void close(Statement stat) {
        if (stat != null) {
            try {
                stat.close();
            } catch (SQLException e) {
            }
        }
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    public static void close(Closeable closeabled) {
        if (closeabled != null) {
            try {
                closeabled.close();
            } catch (IOException e) {
            }
        }
    }

    public static String getSQL(Class<?> clazz, String id, Object... args)
            throws RuntimeException {
        Properties props = sqls.get(clazz);
        if (props == null && clazz != null) {
            props = new Properties();
            String name = clazz.getSimpleName();
            InputStream is = clazz.getResourceAsStream(name + ".sql.xml");
            try {
                props.loadFromXML(is);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                close(is);
            }
            sqls.put(clazz, props);
        }
        return String.format(props.getProperty(id), args);
    }

    private static String getTableName(Class<?> clazz) {
        Alias table = clazz.getAnnotation(Alias.class);
        String tableName = table == null ? null : table.value();
        if (tableName == null) {
            tableName = clazz.getSimpleName();
        }
        return tableName;
    }

    public static SQLEntry getQueryEntry(Object bean) {
        return getQueryEntry(getTableName(bean.getClass()),
                Reflects.toMap(bean));
    }

    public static SQLEntry getQueryEntry(String tableName,
            Map<String, Object> values) {
        SQLEntry sqlEntry = new SQLEntry();
        sqlEntry.sql = new StringBuffer(512);
        sqlEntry.sql.append("select ");
        List<Object> list = new ArrayList<Object>();
        Object value = null;
        String name = null;
        StringBuffer condition = new StringBuffer(128);
        for (Entry<String, Object> entry : values.entrySet()) {
            name = entry.getKey();
            sqlEntry.sql.append(name).append(", ");
            value = entry.getValue();
            if (value != null) {
                condition.append(" and ").append(name).append(" = ?");
                list.add(value);
            }
        }
        sqlEntry.sql.setLength(sqlEntry.sql.length() - 2);
        sqlEntry.sql.append(" from ").append(tableName).append(" where 1 = 1");
        sqlEntry.sql.append(condition);
        sqlEntry.values = list.toArray();
        return sqlEntry;
    }

    public static SQLEntry getUpdateEntry(Object newBean, Object oldBean) {
        return getUpdateEntry(getTableName(newBean.getClass()),
                Reflects.toMap(newBean), Reflects.toMap(oldBean));
    }

    public static SQLEntry getUpdateEntry(String tableName,
            Map<String, Object> newValues, Map<String, Object> oldValues) {
        SQLEntry updateEntry = new SQLEntry();
        updateEntry.sql = new StringBuffer(512);
        updateEntry.sql.append("update ").append(tableName).append(" set ");
        List<Object> values = new ArrayList<Object>();
        Object value = null;
        for (Entry<String, Object> entry : newValues.entrySet()) {
            value = entry.getValue();
            if (value != null) {
                updateEntry.sql.append(entry.getKey()).append(" = ?, ");
                values.add(value);
            }
        }
        updateEntry.sql.setLength(updateEntry.sql.length() - 2);
        updateEntry.sql.append(" where 1 = 1");
        for (Entry<String, Object> entry : oldValues.entrySet()) {
            value = entry.getValue();
            if (value != null) {
                updateEntry.sql.append(" and ").append(entry.getKey())
                        .append(" = ?");
                values.add(value);
            }
        }
        updateEntry.values = values.toArray();
        return updateEntry;
    }

    public static SQLEntry getDeleteEntry(Object bean) {
        return getDeleteEntry(getTableName(bean.getClass()),
                Reflects.toMap(bean));
    }

    public static SQLEntry getDeleteEntry(String tableName,
            Map<String, Object> values) {
        SQLEntry deleteEntry = new SQLEntry();
        deleteEntry.sql = new StringBuffer(512);
        deleteEntry.sql.append("delete ").append(tableName)
                .append(" where 1 = 1");
        List<Object> list = new ArrayList<Object>();
        Object value = null;
        for (Entry<String, Object> entry : values.entrySet()) {
            value = entry.getValue();
            if (value != null) {
                deleteEntry.sql.append(" and ").append(entry.getKey())
                        .append(" = ?");
                list.add(value);
            }
        }
        deleteEntry.values = list.toArray();
        return deleteEntry;
    }

    public static SQLEntry getInsertEntry(Object bean) {
        return getInsertEntry(getTableName(bean.getClass()),
                Reflects.toMap(bean));
    }

    public static SQLEntry getInsertEntry(String tableName,
            Map<String, Object> values) {
        SQLEntry insertEntry = new SQLEntry();
        insertEntry.sql = new StringBuffer(512);
        insertEntry.sql.append("insert into ").append(tableName).append("(");
        List<Object> list = new ArrayList<Object>();
        Object value = null;
        StringBuffer sqlParams = new StringBuffer(128);
        for (Entry<String, Object> entry : values.entrySet()) {
            value = entry.getValue();
            if (value != null) {
                insertEntry.sql.append(entry.getKey()).append(", ");
                sqlParams.append("?, ");
                list.add(value);
            }
        }
        insertEntry.sql.setLength(insertEntry.sql.length() - 2);
        insertEntry.sql.append(") values (");
        insertEntry.sql.append(sqlParams);
        insertEntry.sql.setLength(insertEntry.sql.length() - 2);
        insertEntry.sql.append(")");
        insertEntry.values = list.toArray();
        return insertEntry;
    }

    public static class SQLEntry {

        private StringBuffer sql;

        private Object[] values;

        public SQLEntry() {
        }

        public String getSQL() {
            return sql.toString();
        }

        public Object[] getValues() {
            return values;
        }

    }
}
