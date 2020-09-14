package intern.shopadbox.project.service.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import intern.shopadbox.project.domain.Image;
import intern.shopadbox.project.dto.ImageSearchRes;
import intern.shopadbox.project.service.ElasticSearchRestClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ElasticSearchRestClientServiceImpl implements ElasticSearchRestClientService {

    private final RestHighLevelClient restHighLevelClient;

    public ElasticSearchRestClientServiceImpl(final RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    @Override
    public List<ImageSearchRes> searchRequestHandler(final QueryBuilder queryBuilder,
                                                     final String fieldForSorting,
                                                     final int searchStart,
                                                     final int searchSize,
                                                     final String index) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(queryBuilder);

        searchSourceBuilder.sort(fieldForSorting, SortOrder.ASC);

        searchSourceBuilder.from(searchStart);
        searchSourceBuilder.size(searchSize);

        // ES에 요청 보내기
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);

        // ES로 부터 데이터 받기
        SearchResponse searchResponse = null;

        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SearchHits searchHits = searchResponse.getHits();

        List<ImageSearchRes> resultMap = new ArrayList<>();

        searchHits.forEach(hit->{

            Map<String, Object> hitAsMap = hit.getSourceAsMap();

            final ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

            // JSON 역직렬화
            final ImageSearchRes imageSearchRes = mapper.convertValue(hitAsMap, ImageSearchRes.class);
            imageSearchRes.setScore(hit.getScore());

            resultMap.add(imageSearchRes);

        });

        return resultMap;
    }

    public void saveRequestHandler(final Image image, final String index, final String type) throws IOException {

        final String docId = image.getItemId();  //문서명

        // 문서 색인
        final IndexRequest request = new IndexRequest(index, type, docId);
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        request.source(
                mapper.writeValueAsString(image), XContentType.JSON
        );

        IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);

    }
}