package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.mci.web.model.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

@Component
public class SettingRepository extends BaseRepository {

    @Autowired
    public SettingRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations) {
        super(cassandraOperations);
    }

    public Setting findSettingDataByKey(final String key) {

        Select select = QueryBuilder.select().from("settings");
        select.where(QueryBuilder.eq("key", key));

        return cassandraOps.selectOne(select, Setting.class);
    }

    @CacheEvict("mciSettings")
    public void save(Setting setting) {
        cassandraOps.insert(setting);
    }

    @Cacheable({"mciSettings"})
    public Setting findByKey(String key) {
        return findSettingDataByKey(key);
    }
}
