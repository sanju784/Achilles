/*
 * Copyright (C) 2012-2014 DuyHai DOAN
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package info.archinnov.achilles.test.integration.tests;

import static info.archinnov.achilles.configuration.ConfigurationParameters.FORCE_TABLE_CREATION_PARAM;
import static info.archinnov.achilles.configuration.ConfigurationParameters.PREPARED_STATEMENTS_CACHE_SIZE;
import static info.archinnov.achilles.test.integration.entity.CompleteBeanTestBuilder.builder;
import static org.fest.assertions.api.Assertions.assertThat;
import java.util.Arrays;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import info.archinnov.achilles.embedded.CassandraEmbeddedServerBuilder;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.test.integration.entity.CompleteBean;

public class CacheSizeIT {

    private PersistenceManager pm = CassandraEmbeddedServerBuilder
            .withEntities(CompleteBean.class)
            .withKeyspaceName("prepared_statement_cache")
            .withAchillesConfigParams(ImmutableMap.<String, Object>of(FORCE_TABLE_CREATION_PARAM, true, PREPARED_STATEMENTS_CACHE_SIZE, 2))
            .cleanDataFilesAtStartup(true)
            .buildPersistenceManager();


    @Test
    public void should_re_prepare_statements_when_cache_size_exceeded() throws Exception {
        //Given
        CompleteBean bean = builder().id(RandomUtils.nextLong()).name("name").buid();

        CompleteBean managed = pm.persist(bean);

        //When
        managed.setAge(10L);
        pm.update(managed);

        managed.setFriends(Arrays.asList("foo", "bar"));
        pm.update(managed);

        managed.setFollowers(Sets.newHashSet("George", "Paul"));
        pm.update(managed);

        managed.setAge(11L);
        pm.update(managed);

        //Then
        CompleteBean found = pm.find(CompleteBean.class, bean.getId());

        assertThat(found.getAge()).isEqualTo(11L);
        assertThat(found.getName()).isEqualTo("name");
        assertThat(found.getFriends()).containsExactly("foo", "bar");
        assertThat(found.getFollowers()).containsOnly("George", "Paul");
    }
}
