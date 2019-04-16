package ${config_dao_package_name};

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import ${config_package_name}.*;

public interface ${myClass.className}ConfigDao  extends JpaRepository<${myClass.className}, Integer>, QueryDslPredicateExecutor {
}
