package org.skywalking.apm.collector.agentstream.worker.global.dao;

import org.skywalking.apm.collector.core.framework.UnexpectedException;
import org.skywalking.apm.collector.core.stream.Data;
import org.skywalking.apm.collector.storage.define.DataDefine;
import org.skywalking.apm.collector.storage.define.global.GlobalTraceTable;
import org.skywalking.apm.collector.storage.define.node.NodeComponentTable;
import org.skywalking.apm.collector.storage.h2.dao.H2DAO;
import org.skywalking.apm.collector.storage.h2.define.H2SqlEntity;
import org.skywalking.apm.collector.stream.worker.impl.dao.IPersistenceDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pengys5
 */
public class GlobalTraceH2DAO extends H2DAO implements IGlobalTraceDAO, IPersistenceDAO<H2SqlEntity, H2SqlEntity> {
    private final Logger logger = LoggerFactory.getLogger(GlobalTraceH2DAO.class);
    @Override public Data get(String id, DataDefine dataDefine) {
        throw new UnexpectedException("There is no need to merge stream data with database data.");
    }

    @Override public H2SqlEntity prepareBatchUpdate(Data data) {
        throw new UnexpectedException("There is no need to merge stream data with database data.");
    }

    @Override public H2SqlEntity prepareBatchInsert(Data data) {
        Map<String, Object> source = new HashMap<>();
        H2SqlEntity entity = new H2SqlEntity();
        source.put("id", data.getDataString(0));
        source.put(GlobalTraceTable.COLUMN_SEGMENT_ID, data.getDataString(1));
        source.put(GlobalTraceTable.COLUMN_GLOBAL_TRACE_ID, data.getDataString(2));
        source.put(GlobalTraceTable.COLUMN_TIME_BUCKET, data.getDataLong(0));
        logger.debug("global trace source: {}", source.toString());

        String sql = getBatchInsertSql(NodeComponentTable.TABLE, source.keySet());
        entity.setSql(sql);
        entity.setParams(source.values().toArray(new Object[0]));
        return entity;
    }
}
