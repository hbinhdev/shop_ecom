package com.example.datn_shop_ecom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class GHNService {

    private final String TOKEN = "1485531a-3c1d-11f1-a973-aee5264794df"; // Thay bằng token của bạn (lấy từ GHN)
    private final String SHOP_ID = "200008"; // Thay bằng ShopId của bạn (lấy từ GHN)
    private final String API_URL = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Tính phí vận chuyển qua GHN API
     * 
     * @param toDistrictId ID Quận/Huyện của người nhận
     * @param toWardCode   Mã Phường/Xã của người nhận
     * @return Phí vận chuyển (VNĐ)
     */
    public Integer calculateShippingFee(Integer toDistrictId, String toWardCode) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("from_district_id", 61112); // Ví dụ Đống Đa, Hà Nội - Thay bằng ID Quận kho của bạn
            body.put("to_district_id", toDistrictId);
            body.put("to_ward_code", toWardCode);
            body.put("service_type_id", 2); // Dịch vụ Chuẩn (Standard)
            body.put("height", 10);
            body.put("length", 20);
            body.put("weight", 500); // 500 gram (Giày sneaker)
            body.put("width", 15);

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Token", TOKEN)
                    .header("ShopId", SHOP_ID)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("data").path("total").asInt();
            } else {
                System.err.println("GHN API Error: " + response.body());
                return 35000; // Phí dự phòng nếu lỗi
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 35000;
        }
    }

    /**
     * Lấy ID Quận/Huyện từ tên (Demo - Thực tế nên chọn từ dropdown có sẵn ID)
     */
    /**
     * Tìm ID Quận/Huyện bằng cách gọi API của GHN
     */
    public Integer findDistrictId(String provinceName, String districtName) {
        try {
            String cleanP = cleanName(provinceName);
            String cleanD = cleanName(districtName);

            // 1. Lấy ID Tỉnh trước
            Integer provinceId = null;
            HttpRequest pReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/province"))
                    .header("Token", TOKEN)
                    .GET()
                    .build();
            HttpResponse<String> pRes = httpClient.send(pReq, HttpResponse.BodyHandlers.ofString());
            if (pRes.statusCode() == 200) {
                JsonNode pData = objectMapper.readTree(pRes.body()).path("data");
                for (JsonNode p : pData) {
                    String ghnP = cleanName(p.path("ProvinceName").asText());
                    if (cleanP.contains(ghnP) || ghnP.contains(cleanP)) {
                        provinceId = p.path("ProvinceID").asInt();
                        break;
                    }
                }
            }

            if (provinceId == null) return 1454;

            // 2. Tìm ID Quận
            HttpRequest dReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/district?province_id=" + provinceId))
                    .header("Token", TOKEN)
                    .GET()
                    .build();
            HttpResponse<String> dRes = httpClient.send(dReq, HttpResponse.BodyHandlers.ofString());
            if (dRes.statusCode() == 200) {
                JsonNode dData = objectMapper.readTree(dRes.body()).path("data");
                for (JsonNode d : dData) {
                    String ghnD = cleanName(d.path("DistrictName").asText());
                    if (cleanD.contains(ghnD) || ghnD.contains(cleanD)) {
                        return d.path("DistrictID").asInt();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1454;
    }

    /**
     * Tìm Ward Code bằng cách gọi API của GHN
     */
    public String findWardCode(Integer districtId, String wardName) {
        if (districtId == null || wardName == null || wardName.isBlank()) return "20101";
        try {
            String cleanW = cleanName(wardName);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/ward?district_id=" + districtId))
                    .header("Token", TOKEN)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode data = objectMapper.readTree(response.body()).path("data");
                for (JsonNode w : data) {
                    String ghnW = cleanName(w.path("WardName").asText());
                    if (cleanW.contains(ghnW) || ghnW.contains(cleanW)) {
                        return w.path("WardCode").asText();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "20101";
    }

    public String getProvinces() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/province"))
                .header("Token", TOKEN)
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public String getDistricts(Integer provinceId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/district?province_id=" + provinceId))
                .header("Token", TOKEN)
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public String getWards(Integer districtId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/ward?district_id=" + districtId))
                .header("Token", TOKEN)
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private String cleanName(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace("thành phố", "")
                .replace("tỉnh", "")
                .replace("quận", "")
                .replace("huyện", "")
                .replace("thị xã", "")
                .replace("phường", "")
                .replace("xã", "")
                .replace("thị trấn", "")
                .trim();
    }
}
