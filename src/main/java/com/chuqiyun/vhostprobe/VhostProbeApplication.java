package com.chuqiyun.vhostprobe;

import com.chuqiyun.vhostprobe.config.FilterFileConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author mryunqi
 */
@SpringBootApplication
@EnableScheduling
public class VhostProbeApplication {
    private static FilterFileConfig filterFileConfig;

    @Autowired
    public void setFilterFileConfig(FilterFileConfig filterFileConfig) {
        VhostProbeApplication.filterFileConfig = filterFileConfig;
    }

    public static void main(String[] args) {
        SpringApplication.run(VhostProbeApplication.class, args);
        filterFileConfig.fileFilter("D:/Project/初七云/宝塔主控/log");
    }

}
