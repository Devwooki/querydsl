package devwooki.study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import devwooki.study.querydsl.entity.Hello;
import devwooki.study.querydsl.entity.QHello;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional //기본적으로 rollback 시켜버린다.
@Commit //을 작성해줌으로써, create해도 사라지지 않고 조회가 된다.
class QuerydslApplicationTests {

	@Autowired
	//@PersistenceContext //자바 표준 스펙에선 명시해주자 : 다른 프레임워크로 바꿀 것 같으면 사용
	EntityManager em;

	//java 즉 intellij가 직접 빌드하도록 setting -> build, execution -> build tool -> gradle
	// build and run using, test using intellij 가 하게 변경
	@Test
	void contextLoads() {
		Hello hello = new Hello();
		em.persist(hello);

		JPAQueryFactory query = new JPAQueryFactory(em);
		//QHello qHello = new QHello("h"); //alias 삽입
		// 아래도 동일한 타입이다. QHello는 Static으로 선언되어있기 때문
		QHello qHello = QHello.hello;

		//query와 관련된건 모두 Q타입을 삽입해야한다.
		Hello result = query.selectFrom(qHello).fetchOne();

		//result 객체가 hello와 같은지 검증
		//alt + enter를 통해 import한 객체를 static으로도 사용할 수 있음
		//option + enter
		assertThat(result).isEqualTo(hello);
		assertThat(result.getId()).isEqualTo(hello.getId());
	}

	@Test
	void apiTest(){
	}

}
