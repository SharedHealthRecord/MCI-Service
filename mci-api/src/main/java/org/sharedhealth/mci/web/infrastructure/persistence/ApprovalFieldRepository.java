package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.mci.web.model.ApprovalField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

@Component
public class ApprovalFieldRepository extends BaseRepository {

    @Autowired
    public ApprovalFieldRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations) {
        super(cassandraOperations);
    }

    public ApprovalField findFieldDataByKey(final String field) {

        Select select = QueryBuilder.select().from("approval_fields");
        select.where(QueryBuilder.eq("field", field));

        return cassandraOps.selectOne(select, ApprovalField.class);
    }

    @CacheEvict(value = { "mciApprovalFields" }, allEntries = true)
    public void resetAllCache() {}

    @CacheEvict(value = { "mciApprovalFields" }, key = "#field")
    public void resetCacheByKey(String field) {}

    public void save(ApprovalField approvalField) {
        resetCacheByKey(approvalField.getField());
        cassandraOps.insert(approvalField);
    }

    @Cacheable({"mciApprovalFields"})
    public ApprovalField findByField(String field) {
        return findFieldDataByKey(field);
    }
}
