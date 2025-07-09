package com.millo.ilhayoung.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.core.convert.DbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver

@Configuration
class MongoConfig {
    
    /**
     * MongoDB 타입 매핑 설정
     * _class 필드를 저장하지 않도록 설정
     */
    @Bean
    fun mappingMongoConverter(
        mongoDbFactory: MongoDatabaseFactory,
        mongoMappingContext: MongoMappingContext,
        customConversions: MongoCustomConversions
    ): MappingMongoConverter {
        val dbRefResolver: DbRefResolver = DefaultDbRefResolver(mongoDbFactory)
        val converter = MappingMongoConverter(dbRefResolver, mongoMappingContext)
        converter.setCustomConversions(customConversions)
        // _class 필드를 저장하지 않도록 설정
        converter.setTypeMapper(DefaultMongoTypeMapper(null))
        return converter
    }
} 