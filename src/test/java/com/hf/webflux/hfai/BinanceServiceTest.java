package com.hf.webflux.hfai;

import java.util.*;
import java.math.*;

import com.hf.webflux.hfai.cex.BinanceService;
import com.hf.webflux.hfai.cex.SymbolConstant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

@SpringBootTest
public class BinanceServiceTest {




    @Test
    public void getKline_FetchDataError_ThrowsRuntimeException() {

    }
}

