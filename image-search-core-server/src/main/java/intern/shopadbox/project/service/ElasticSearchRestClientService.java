package intern.shopadbox.project.service;

import intern.shopadbox.project.domain.Image;
import intern.shopadbox.project.dto.ImageSearchRes;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.List;

public interface ElasticSearchRestClientService {

    List<ImageSearchRes> searchRequestHandler(final QueryBuilder queryBuilder,
                                              final String fieldForSorting,
                                              final int searchStart,
                                              final int searchSize,
                                              final String index);

    void saveRequestHandler(final Image image, final String index, final String type) throws IOException;

}