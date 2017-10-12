package org.skywalking.apm.collector.ui.dao;

import com.google.gson.JsonArray;
import org.skywalking.apm.collector.client.h2.H2Client;
import org.skywalking.apm.collector.client.h2.H2ClientException;
import org.skywalking.apm.collector.core.util.Const;
import org.skywalking.apm.collector.core.util.TimeBucketUtils;
import org.skywalking.apm.collector.storage.define.instance.InstPerformanceTable;
import org.skywalking.apm.collector.storage.define.jvm.CpuMetricTable;
import org.skywalking.apm.collector.storage.define.register.InstanceDataDefine;
import org.skywalking.apm.collector.storage.define.register.InstanceTable;
import org.skywalking.apm.collector.storage.h2.dao.H2DAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pengys5
 */
public class InstPerformanceH2DAO extends H2DAO implements IInstPerformanceDAO {
    private final Logger logger = LoggerFactory.getLogger(InstPerformanceH2DAO.class);
    private static final String GET_INST_PERF_SQL = "select * from {0} where {1} = ? and {2} in (";
    private static final String GET_TPS_METRIC_SQL = "select * from {0} where {1} = ?";
    private static final String GET_TPS_METRICS_SQL = "select * from {0} where {1} in (";
    @Override public InstPerformance get(long[] timeBuckets, int instanceId) {
        H2Client client = getClient();
        logger.info("the inst performance inst id = {}", instanceId);
        String sql = MessageFormat.format(GET_INST_PERF_SQL, InstPerformanceTable.TABLE, InstPerformanceTable.COLUMN_INSTANCE_ID, InstPerformanceTable.COLUMN_TIME_BUCKET);
        StringBuilder builder = new StringBuilder();
        for( int i = 0 ; i < timeBuckets.length; i++ ) {
            builder.append("?,");
        }
        builder.delete(builder.length() - 1, builder.length());
        builder.append(")");
        sql = sql + builder;
        Object[] params = new Object[timeBuckets.length + 1];
        for(int i = 0; i < timeBuckets.length; i++) {
            params[i + 1] = timeBuckets[i];
        }
        params[0] = instanceId;
        try (ResultSet rs = client.executeQuery(sql, params)) {
            if (rs.next()) {
                int callTimes = rs.getInt(InstPerformanceTable.COLUMN_CALLS);
                int costTotal = rs.getInt(InstPerformanceTable.COLUMN_COST_TOTAL);
                return new InstPerformance(instanceId, callTimes, costTotal);
            }
        } catch (SQLException | H2ClientException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override public int getTpsMetric(int instanceId, long timeBucket) {
        H2Client client = getClient();
        String sql = MessageFormat.format(GET_TPS_METRIC_SQL, InstPerformanceTable.TABLE, "id");
        Object[] params = new Object[]{instanceId};
        try (ResultSet rs = client.executeQuery(sql, params)) {
            if (rs.next()) {
                return rs.getInt(InstPerformanceTable.COLUMN_CALLS);
            }
        } catch (SQLException | H2ClientException e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    @Override public JsonArray getTpsMetric(int instanceId, long startTimeBucket, long endTimeBucket) {
        H2Client client = getClient();
        String sql = MessageFormat.format(GET_TPS_METRICS_SQL, InstPerformanceTable.TABLE, "id");

        long timeBucket = startTimeBucket;
        List<String> idList = new ArrayList<>();
        do {
            String id = timeBucket + Const.ID_SPLIT + instanceId;
            timeBucket = TimeBucketUtils.INSTANCE.addSecondForSecondTimeBucket(TimeBucketUtils.TimeBucketType.SECOND.name(), timeBucket, 1);
            idList.add(id);
        }
        while (timeBucket <= endTimeBucket);

        StringBuilder builder = new StringBuilder();
        for( int i = 0 ; i < idList.size(); i++ ) {
            builder.append("?,");
        }
        builder.delete(builder.length() - 1, builder.length());
        builder.append(")");
        sql = sql + builder;
        Object[] params = idList.toArray(new String[0]);

        JsonArray metrics = new JsonArray();
        try (ResultSet rs = client.executeQuery(sql, params)) {
            while (rs.next()) {
                int calls = rs.getInt(InstPerformanceTable.COLUMN_CALLS);
                metrics.add(calls);
            }
        } catch (SQLException | H2ClientException e) {
            logger.error(e.getMessage(), e);
        }
        return metrics;
    }

    @Override public int getRespTimeMetric(int instanceId, long timeBucket) {
        H2Client client = getClient();
        String sql = MessageFormat.format(GET_TPS_METRIC_SQL, InstPerformanceTable.TABLE, "id");
        Object[] params = new Object[]{instanceId};
        try (ResultSet rs = client.executeQuery(sql, params)) {
            if (rs.next()) {
                int callTimes = rs.getInt(InstPerformanceTable.COLUMN_CALLS);
                int costTotal = rs.getInt(InstPerformanceTable.COLUMN_COST_TOTAL);
                return costTotal / callTimes;
            }
        } catch (SQLException | H2ClientException e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    @Override public JsonArray getRespTimeMetric(int instanceId, long startTimeBucket, long endTimeBucket) {
        H2Client client = getClient();
        String sql = MessageFormat.format(GET_TPS_METRICS_SQL, InstPerformanceTable.TABLE, "id");

        long timeBucket = startTimeBucket;
        List<String> idList = new ArrayList<>();
        do {
            String id = timeBucket + Const.ID_SPLIT + instanceId;
            timeBucket = TimeBucketUtils.INSTANCE.addSecondForSecondTimeBucket(TimeBucketUtils.TimeBucketType.SECOND.name(), timeBucket, 1);
            idList.add(id);
        }
        while (timeBucket <= endTimeBucket);

        StringBuilder builder = new StringBuilder();
        for( int i = 0 ; i < idList.size(); i++ ) {
            builder.append("?,");
        }
        builder.delete(builder.length() - 1, builder.length());
        builder.append(")");
        sql = sql + builder;
        Object[] params = idList.toArray(new String[0]);

        JsonArray metrics = new JsonArray();
        try (ResultSet rs = client.executeQuery(sql, params)) {
            while (rs.next()) {
                int callTimes = rs.getInt(InstPerformanceTable.COLUMN_CALLS);
                int costTotal = rs.getInt(InstPerformanceTable.COLUMN_COST_TOTAL);
                metrics.add(costTotal / callTimes);
            }
        } catch (SQLException | H2ClientException e) {
            logger.error(e.getMessage(), e);
        }
        return metrics;
    }
}
