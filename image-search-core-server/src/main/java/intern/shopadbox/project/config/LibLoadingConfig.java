package intern.shopadbox.project.config;

import org.springframework.context.annotation.Configuration;

/**
 * 외부 라이브러리 로딩 설정 클래스
 */

@Configuration
public class LibLoadingConfig {

    static {
        nu.pattern.OpenCV.loadLocally();
    }

}
