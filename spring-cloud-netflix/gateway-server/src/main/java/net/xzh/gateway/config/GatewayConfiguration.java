package net.xzh.gateway.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;

import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfiguration {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public GatewayConfiguration(ObjectProvider<List<ViewResolver>> viewResolversProvider, ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    /**
     * ??????????????????????????????:SentinelGatewayBlockExceptionHandler
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers,serverCodecConfigurer);
    }
    /**
     * ?????????????????????
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }
    /**
     * ??????????????????????????????
     */
    @PostConstruct
    public void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("user-service").setCount(1).setIntervalSec(1));
        
        rules.add(new GatewayFlowRule("user_api").setCount(1).setIntervalSec(1));
        rules.add(new GatewayFlowRule("feign_api").setCount(1).setIntervalSec(1));
        GatewayRuleManager.loadRules(rules);
    }

    // ?????????API??????
    @PostConstruct
    private void initCustomizedApis() {
    	Set<ApiDefinition> definitions = new HashSet<>();
		ApiDefinition api1 = new ApiDefinition("user_api")
				.setPredicateItems(new HashSet<ApiPredicateItem>() {{
					add(new ApiPathPredicateItem().setPattern("/user-service/user/**"). //???/user-service/user/???????????????url
							setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
				}});
		ApiDefinition api2 = new ApiDefinition("feign_api")
				.setPredicateItems(new HashSet<ApiPredicateItem>() {{
					add(new ApiPathPredicateItem().setPattern("/feign-service/user/getByUsername")); //????????????/feign-service/user/getByUsername???url
				}});
		definitions.add(api1);
		definitions.add(api2);
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }

    //?????????????????????
    @PostConstruct
    public void initBlockHandlers() {
        BlockRequestHandler blockRequestHandler = new BlockRequestHandler() {
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
                Map map = new HashMap<>();
                map.put("code", 500);
                map.put("message", "???????????????????????????");
                return ServerResponse.status(HttpStatus.OK).//?????????200
                        contentType(MediaType.APPLICATION_JSON_UTF8).//application/json;charset=UTF-8
                        body(BodyInserters.fromObject(map));
            }
        };
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }
}