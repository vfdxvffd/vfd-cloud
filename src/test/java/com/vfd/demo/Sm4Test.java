package com.vfd.demo;

import com.vfd.demo.controller.UploadFileController;
import com.vfd.demo.service.FileOperationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @PackageName: com.vfd.demo
 * @ClassName: Sm4Test
 * @Description:
 * @author: vfdxvffd
 * @date: 2021/2/2 下午8:57
 */
@SpringBootTest
public class Sm4Test {

    @Autowired
    FileOperationService fileOperationService;

    @Test
    void decrypt () {
        fileOperationService.decryptFile(UploadFileController.PROJECT_DIR+"29/~tmp", 29);
    }
}
