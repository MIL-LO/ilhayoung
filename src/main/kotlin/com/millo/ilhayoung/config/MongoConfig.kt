package com.millo.ilhayoung.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.EnableMongoAuditing

/**
 * MongoDB 관련 설정을 담당하는 클래스
 * MongoDB auditing 기능을 활성화하여 BaseDocument의 생성/수정 시간을 자동으로 관리
 */
@Configuration
@EnableMongoAuditing
class MongoConfig 