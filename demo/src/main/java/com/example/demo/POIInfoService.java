package com.example.demo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import utils.HttpClient;
import java.util.HashMap;
import java.util.Map;

/**
 * 根据用户输入信息获取 获取POI信息
 */
@Component
public class POIInfoService {

    private final static Logger logger = LoggerFactory.getLogger(POIInfoService.class);
    @Value("${poiSearch.geoCoderUrl}")
    private  String geoCoderUrl;
    @Value("${poiSearch.poiSearchUrl}")
    private  String poiSearchUrl;
    @Value("${poiSearch.ak}")
    private  String ak;
    @Value("${poiSearch.ak}")
    private  String radius;
    @Value("${poiSearch.filter}")
    private  String filter;
    @Value("${poiSearch.poiName1}")
    private  String query1;
    @Value("${poiSearch.poiName2}")
    private  String query2;
    private final String output = "json";// 统一使用json
    private final String scope = "2";// 使用百度返回结果过滤

    /**
     *  查询4s店
     * @param inputAdr 用户输入地理位置信息
     * @return 五公里内最近的福特4s店名称
     */
    public String get4sShop (String inputAdr) {
       String geoCoderUrl =  getGeocoder(inputAdr);
        if(StringUtils.isEmpty(geoCoderUrl)) {
            return null;
        }
        return getNearestPOI(geoCoderUrl,query1);
    }

    /**
     *  查询加油站
     * @param inputAdr 用户输入地理位置信息
     * @return 五公里内最近的中石化加油站名称
     */
    public String getGasStation (String inputAdr) {
        String geoCoderUrl =  getGeocoder(inputAdr);
        if(StringUtils.isEmpty(geoCoderUrl)) {
            return null;
        }
        return getNearestPOI(geoCoderUrl,query2);
    }

    /**
     * 获取地理编码
     * @param inputStr 用户输入地址
     * @throws Exception
     */
    private  String getGeocoder(String inputStr){
        // 参数拼接
        Map<String, Object> map = new HashMap<String,Object>();
        map.put("address",inputStr);
        map.put("output",output);
        map.put("ak",ak);
        // http调用
        String result;
        int status;
        JSONObject jsonObject = null;
        try {
            result = HttpClient.doPost(geoCoderUrl,map);
            jsonObject = (JSONObject) JSONObject.parse(result);
            status = (int) jsonObject.get("status");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取用户输入地址的地理坐标异常",e);
            return null;
        }
        // 返回解析
        if(0 != status) {
            logger.info("获取用户输入地址的地理坐标失败："+jsonObject.toJSONString());
            return null;
        }
        JSONObject resultObj = jsonObject.getJSONObject("result");
        JSONObject locationObj = resultObj.getJSONObject("location");
        String lat = locationObj.getString("lat");
        String lng = locationObj.getString("lng");
        logger.debug("输入地址的地理坐标是："+ lat+" & " + lng);
        return lat + ","+ lng; // 返回输入坐标地址的经纬度
    }

    /**
     * 获取最近的POI
     * @param location 地理坐标
     * @param query 查询poi
     * @return 地理坐标中文名称
     * @throws Exception
     */
    private  String getNearestPOI(String location,String query) {
        // 参数拼接
        StringBuffer paramUrl = new StringBuffer(poiSearchUrl);
        paramUrl.append("?query=").append(query).append("&location=").append(location).
                append("&radius=").append(radius).append("&scope=").append(scope).append("&output=")
                .append(output).append("&filter=").append(filter).append("&ak=").append(ak);
        // http调用
        String res;
        int status;
        JSONObject jsonObj;
        try {
            res = HttpClient.doGet(paramUrl.toString());
            jsonObj = (JSONObject) JSONObject.parse(res);
            status = (int) jsonObj.get("status");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("区域地点检索异常",e);
            return null;
        }
        if (0 != status) {
            logger.info("区域地点检索失败："+jsonObj.toJSONString());
            return null;
        }
        // 返回解析
        JSONArray resultObj = jsonObj.getJSONArray("results");
        if(resultObj.size() == 0){
            return null;
        }
        JSONObject obj = (JSONObject) resultObj.get(0);
        String name = (String) obj.get("name");
        return name;
    }

}
