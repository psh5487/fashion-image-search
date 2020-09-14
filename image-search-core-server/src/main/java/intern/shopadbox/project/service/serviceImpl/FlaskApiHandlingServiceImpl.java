package intern.shopadbox.project.service.serviceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import intern.shopadbox.project.domain.Image;

import com.fasterxml.jackson.databind.ObjectMapper;
import intern.shopadbox.project.service.FlaskApiHandlingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FlaskApiHandlingServiceImpl implements FlaskApiHandlingService {

    @Value("${python.flask.address}")
    private String flask_address;

    /*
     *  Python Server에서 TF 특징점(imagenet_feature) 추출하고, ES 의 'img_list' 에 저장
     */
    @Override
    public void saveImagenetFeature(Image image) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("item_id", image.getItemId());
        map.put("img_url", image.getImgUrl());

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
        StringEntity entity = new StringEntity(json);

        HttpClient client = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(flask_address + "saveImagenetFeature");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        client.execute(httpPost);

    }

    @Override
    public void saveAllImageInfo(Image image) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("item_id", image.getItemId());
        map.put("item_name", image.getItemName());
        map.put("category_id", image.getCategoryId());
        map.put("img_url", image.getImgUrl());

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);

        StringEntity entity = new StringEntity(json);

        HttpClient client = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(flask_address + "saveAllImageInfo");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        client.execute(httpPost);
    }

    /*
     *  Python Server에 imgUrl로 요청을 보내면 imagenet_feature 받아오기
     */
    @Override
    public float[] getImagenetFeature(String imgUrl) throws IOException{

        HttpClient client = HttpClientBuilder.create().build();

        HttpGet getRequest = new HttpGet(flask_address + "getImagenetFeature?img_url=" + imgUrl);
        HttpResponse response = client.execute(getRequest);
        ResponseHandler<String> handler = new BasicResponseHandler();
        String jsonStr = handler.handleResponse(response);

        //json -> Map
        ObjectMapper obm  = new ObjectMapper();
        Map<String, float[] > map = obm.readValue(jsonStr, new TypeReference<Map<String, float[]>>() {});

        float[] imagenet_feature = map.get("imagenet_feature");

        return imagenet_feature;
    }

}