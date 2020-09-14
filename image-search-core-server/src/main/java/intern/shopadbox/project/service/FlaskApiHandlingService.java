package intern.shopadbox.project.service;

import intern.shopadbox.project.domain.Image;

import java.io.IOException;

public interface FlaskApiHandlingService {

    void saveImagenetFeature(Image image) throws IOException;

    void saveAllImageInfo(Image image) throws IOException;

    float[] getImagenetFeature(String imgUrl) throws IOException;

}