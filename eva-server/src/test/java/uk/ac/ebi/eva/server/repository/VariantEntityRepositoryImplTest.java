///*
// * European Variation Archive (EVA) - Open-access database of all types of genetic
// * variation data from all species
// *
// * Copyright 2016 EMBL - European Bioinformatics Institute
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package uk.ac.ebi.eva.server.repository;
//
//import com.mongodb.DB;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.dao.DataAccessException;
//import org.springframework.dao.support.PersistenceExceptionTranslator;
//import org.springframework.data.mongodb.MongoDbFactory;
//import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration
//@ComponentScan(basePackageClasses = { VariantEntityRepositoryImpl.class })
//public class VariantEntityRepositoryImplTest {
//
//    @Autowired
//    private ApplicationContext applicationContext;
//
//    @Autowired
//    private VariantEntityRepositoryImpl variantEntityRepositoryImpl;
//
//    private Query testQueryA;
//    private Query testQueryB;
//
//    @Before
//    public void setUp() {
//        this.testQueryA = new Query(Criteria
//                                           .where("chr").is("1")
//                                           .and("start").lte(1).gt(-1000000)
//                                           .and("end").gte(1).lt(1000000)
//        );
//
//        this.testQueryB = new Query(Criteria
//                                            .where("chr").is("1")
//                                            .and("start").lte(1).gt(-1000000)
//                                            .and("end").gte(1).lt(1000000)
//        );
//    }
//
//    @Test
//    public void queryConsequenceTypeValid() throws Exception {
//        List<String> consequenceType = new ArrayList<>();
//        consequenceType.add("SO:0001234");
//        variantEntityRepositoryImpl.queryConsequenceType(testQueryB, consequenceType);
//        testQueryA.addCriteria(Criteria.where("annot.ct.so").in("1234"));
//
//        assertEquals(testQueryA, testQueryB);
//    }
//
//    @Test
//    public void queryMaf() throws Exception {
//
//    }
//
//    @Test
//    public void queryPolyphenScore() throws Exception {
//
//    }
//
//    @Test
//    public void querySift() throws Exception {
//
//    }
//
//    @Test
//    public void queryStudies() throws Exception {
//
//    }
//}