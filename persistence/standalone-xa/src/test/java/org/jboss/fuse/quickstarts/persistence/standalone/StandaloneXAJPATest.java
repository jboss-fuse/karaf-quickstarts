/*
 *  Copyright 2005-2018 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package org.jboss.fuse.quickstarts.persistence.standalone;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.common.util.propertyservice.PropertiesFactory;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.DataSourceConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.apache.commons.dbcp2.managed.PoolableManagedConnectionFactory;
import org.apache.commons.dbcp2.managed.TransactionRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.jboss.fuse.quickstarts.persistence.standalone.model.TestEntity;
import org.jboss.narayana.osgi.jta.internal.OsgiTransactionManager;
import org.jboss.tm.XAResourceRecovery;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.jdbc.common.BeanConfig;
import org.ops4j.pax.jdbc.pool.narayana.impl.DbcpXAPooledDataSourceFactory;
import org.osgi.framework.ServiceRegistration;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.xa.PGXADataSource;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class StandaloneXAJPATest {

    public static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StandaloneXAJPATest.class);

    private UserTransaction userTransaction;
    private TransactionManager transactionManager;

    @Before
    public void init() throws SystemException {
        File txDir = new File("target/tx");
        FileUtils.deleteQuietly(txDir);
        txDir.mkdirs();

        Properties properties = PropertiesFactory.getDefaultProperties();
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.objectStoreType", "com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.objectStoreDir", "target/tx");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.localOSRoot", "defaultStore");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.communicationStore.objectStoreType", "com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.communicationStore.objectStoreDir", "target/tx");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.communicationStore.localOSRoot", "communicationStore");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.stateStore.objectStoreType", "com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.stateStore.objectStoreDir", "target/tx");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.stateStore.localOSRoot", "stateStore");

        // Arjuna/Narayana objects
        JTAEnvironmentBean env = jtaPropertyManager.getJTAEnvironmentBean();
        OsgiTransactionManager tmimpl = new OsgiTransactionManager();
        tmimpl.setTransactionTimeout(3600);
        env.setUserTransaction(tmimpl);
        env.setTransactionManager(tmimpl);

        // javax.transaction API
        this.userTransaction = tmimpl;
        this.transactionManager = tmimpl;
    }

    @Test
    public void entityManagerAccess() throws Exception {

        // database-specific, non-pooling, non-enlisting javax.sql.XADataSource
        JdbcDataSource xaH2 = new JdbcDataSource();
        xaH2.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        xaH2.setUser("fuse");
        xaH2.setPassword("fuse");

        // non database-specific, pooling, enlisting javax.sql.DataSource

        DataSourceXAConnectionFactory connFactory = new DataSourceXAConnectionFactory(transactionManager, xaH2);
        PoolableManagedConnectionFactory pcf = new PoolableManagedConnectionFactory(connFactory, null);
        GenericObjectPoolConfig<PoolableConnection> conf = new GenericObjectPoolConfig<>();

        conf.setMinIdle(2);
        conf.setMaxTotal(10);
        conf.setBlockWhenExhausted(true);

        GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<PoolableConnection>(pcf, conf);
        pcf.setPool(pool);
        TransactionRegistry transactionRegistry = connFactory.getTransactionRegistry();
        ManagedDataSource<PoolableConnection> mds = new ManagedDataSource<PoolableConnection>(pool, transactionRegistry);

        javax.sql.DataSource applicationDataSource = mds;

        // we have javax.sql.DataSource and javax.transaction.TransactionManager now

        try (Connection c = applicationDataSource.getConnection()) {
            try (Statement st = c.createStatement()) {
                st.execute("create table TEST_ENTITY (ID bigint not null, DESCRIPTION varchar(255), primary key (ID))");
            }
            PreparedStatement ps = c.prepareStatement("insert into TEST_ENTITY (DESCRIPTION, ID) values (?, ?)");
            ps.setString(1, "Record 1");
            ps.setInt(2, 1);
            ps.execute();
        }

        userTransaction.begin();

        try (Connection c = applicationDataSource.getConnection()) {
            try (Statement st = c.createStatement()) {
                try (ResultSet rs = st.executeQuery("select testentity0_.ID as ID1_0_, testentity0_.DESCRIPTION as DESCRIPT2_0_ from TEST_ENTITY testentity0_")) {
                    while (rs.next()) {
                        LOG.info(String.format("%d: %s", rs.getLong("ID1_0_"), rs.getString("DESCRIPT2_0_")));
                    }
                }
            }
        }

        try (Connection c = applicationDataSource.getConnection()) {
            try (Statement st = c.createStatement()) {
                st.execute("delete from TEST_ENTITY");
            }
        }

        userTransaction.commit();

        Properties properties = new Properties();
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        MutablePersistenceUnitInfo pinfo = new MutablePersistenceUnitInfo() {
            @Override
            public ClassLoader getNewTempClassLoader() {
                return StandaloneXAJPATest.class.getClassLoader();
            }
        };
        pinfo.setJtaDataSource(applicationDataSource);
        pinfo.setTransactionType(PersistenceUnitTransactionType.JTA);
        pinfo.setPersistenceUnitName("fuse.jpa.jta.test.JpaPersistenceUnit");
        pinfo.setProperties(properties);
        pinfo.addManagedClassName(TestEntity.class.getName());
        EntityManagerFactory emf = new HibernatePersistenceProvider().createContainerEntityManagerFactory(pinfo, properties);

        List<TestEntity> result = null;

//        userTransaction.begin();
//
//        try (Connection c = applicationDataSource.getConnection()) {
//            PreparedStatement ps = c.prepareStatement("insert into TEST_ENTITY (DESCRIPTION, ID) values (?, ?)");
//            ps.setString(1, "Record 1");
//            ps.setInt(2, 1);
//            ps.execute();
//        }
//
//        em.joinTransaction();
//        result = em.createQuery("from TestEntity", TestEntity.class).getResultList();
//        assertThat(result.size(), equalTo(1));
//        for (TestEntity entity : result) {
//            LOG.info("Entity {}: {}", entity.getId(), entity.getDescription());
//        }
//
//        userTransaction.rollback();
//
//        em.clear();

        userTransaction.begin();
        EntityManager em = emf.createEntityManager();
        em.joinTransaction();

        result = em.createQuery("from TestEntity", TestEntity.class).getResultList();
        assertThat(result.size(), equalTo(0));


        em.persist(new TestEntity(3L, "record 3"));
        // either em.flush() or a query is needed (query leads to implicit flush of pending insert)
        em.flush();
//        result = em.createQuery("from TestEntity", TestEntity.class).getResultList();
//        assertThat(result.size(), equalTo(1));
//        for (TestEntity entity : result) {
//            LOG.info("Entity {}: {}", entity.getId(), entity.getDescription());
//        }

        Transaction suspended = transactionManager.suspend();

        transactionManager.begin();
        Transaction newTx = transactionManager.getTransaction();

//        EntityManager em2 = emf.createEntityManager();
//        em2.joinTransaction();
//        assertThat(em2.createQuery("from TestEntity", TestEntity.class).getResultList().size(), equalTo(0));

//        em.joinTransaction();
        assertThat(em.createQuery("from TestEntity", TestEntity.class).getResultList().size(), equalTo(0));

        transactionManager.rollback();
        transactionManager.resume(suspended);

        userTransaction.commit();

        em.clear();
        result = em.createQuery("from TestEntity", TestEntity.class).getResultList();
        assertThat(result.size(), equalTo(1));
        for (TestEntity entity : result) {
            LOG.info("Entity {}: {}", entity.getId(), entity.getDescription());
        }

        em.close();
    }

}
