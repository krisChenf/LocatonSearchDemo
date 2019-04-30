package com.example.demo;

import com.example.demo.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.util.StringUtils;
import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Set;

/**
 * 搜索查询登录 统一控制层
 * Created by chenfei on 2019/4/29.
 */
@RequestMapping("/search")
@RestController
public class SearchQueryController {
    @Autowired
    private POIInfoService service;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private final static String SUFFIX_STATION = "gasStation";
    private final static String SUFFIX_4SSHOP = "4sShop";
    private final static Logger logger = LoggerFactory.getLogger(SearchQueryController.class);

    /**
     * 进入搜索页面
     *
     * @return
     */
    @RequestMapping("/toSearchPage")
    public ModelAndView toSearchPage() {
        return new ModelAndView("search");
    }

    /**
     * 获取5公里内最近的 福特4s店信息
     *
     * @param address 用户输入地址
     * @return 门店中文名称
     * @author chenfei
     */
    @GetMapping("/get4sShop/{address}")
    public String get4sShop(@PathVariable("address") String address, HttpServletRequest request) {
        String addrName = null;
        try {
            addrName = service.get4sShop(address);
            if (StringUtils.isEmpty(addrName)) {
                return "查询结果为空";
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取4s店信息异常", e);
            return "获取4s店信息异常";
        }
        saveToRedis(request, addrName, SUFFIX_4SSHOP);
        return addrName;
    }

    /**
     * 获取5公里内最近的中石化加油站信息
     *
     * @param address 用户输入地址
     * @return 加油站中文名称
     * @author chenfei
     */
    @GetMapping("/getGasStation/{address}")
    public String getGasStation(@PathVariable("address") String address, HttpServletRequest request) {
        String addrName = null;
        try {
            addrName = service.getGasStation(address);
            if (StringUtils.isEmpty(addrName)) {
                return "查询结果为空";
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取加油站信息异常", e);
            return "获取加油站信息异常";
        }
        saveToRedis(request, addrName, SUFFIX_STATION);
        return addrName;
    }

    /**
     * 获取当前用户查询此次最频繁的加油站和4s店
     *
     * @return 加油站和4s店 中文名称
     * @author chenfei 2019年4月29日
     */
    @GetMapping("/getPOI")
    public String getStationAndShop(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        return getPOIString(user.getLogin());
    }
    /**
     * 根据用户login 名称 获取查询次数最频繁的加油站和4s店
     *
     * @return 加油站和4s店 中文名称
     * @author chenfei 2019年4月29日
     */
    @GetMapping("/getPOICustomer/{userLogin}")
    public String getStationShop(@PathVariable("userLogin") String userLogin) {
        if (org.springframework.util.StringUtils.isEmpty(userLogin)) {
            logger.info("无法获取用户登录名 userLogin");
            return null;
        }
        return getPOIString(userLogin);
    }

    private String getPOIString(String userLogin) {
        String stationKey = userLogin + SUFFIX_STATION;
        String shopKey = userLogin + SUFFIX_4SSHOP;
        String stationName,shopName;
        try {
            ZSetOperations<String, String> zops = redisTemplate.opsForZSet();
            // 倒叙取出 加油站数据记录集合
            Set<String> set = zops.reverseRange(stationKey, 0, -1);
            if (0 == set.size()) {
                return "查询记录为空!";
            }
            Iterator<String> it = set.iterator();
            stationName = it.next();
            // 获取范围的元素来自start于end从下令从高分到低分排序集,0,-1 全部
            Set<String> set2 = zops.reverseRange(shopKey, 0, -1);
            Iterator<String> it2 = set2.iterator();
            shopName = it2.next();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取查询记录异常", e);
            return "获取查询记录异常";
        }
        return stationName + " & " + shopName;
    }

    /**
     * 将查询出的记录结果保存至redis, key为用户名加上记录集合名称（4s店/加油站）
     * 使用zset存储 记录集 不存在添加
     *
     * @param request
     * @param addrName
     * @param recordNme 集合名称
     * @author chenfei 2019年4月29日
     */
    private void saveToRedis(HttpServletRequest request, String addrName, String recordNme) {
        User user = (User) request.getSession().getAttribute("user");
        String key = user.getLogin() + recordNme;
        try {
            ZSetOperations<String, String> zops = redisTemplate.opsForZSet();
            zops.incrementScore(key, addrName, 1d);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询记录保存异常..." + addrName, e);
        }
    }
}