# -*- coding: utf-8 -*-

import tensorflow as tf
import tensorflow_hub as hub

import numpy as np
import time
import requests

from flask import Flask, request, jsonify
from elasticsearch import Elasticsearch

# Flask Server 설정
app = Flask(__name__)
flask_host = "localhost"
flask_port = "5000"

# ElasticSearch Address
es_address = "http://localhost:9200"


'''
이미지 URL 을 통해 이미지 전처리 

299 x 299 x 3 shape tensor 로 전처리된 이미지 반환
'''


def load_img(url):

    # URL 로 부터 이미지 읽기
    # JPEG 이미지를 uint8 W X H X 3 tensor 로 Decode
    img = tf.image.decode_jpeg(requests.get(url).content, channels=3)

    # Resize 299 x 299 x 3 shape tensor
    img = tf.compat.v1.image.resize_image_with_pad(img, 299, 299)

    # new axis 를 추가하여 Data type 을 uint8 에서 float32 로 변환
    # float32 1 x 299 x 299 x 3 tensor (inception_v3 model 에서 요구하는 형태)
    img = tf.image.convert_image_dtype(img, tf.float32)[tf.newaxis, ...]

    return img


'''
이미지 TF 특징점 저장

이미지 URL 을 사용하여 이미지의 TF 특징점을 뽑고, Elasticsearch의 "img_list"에 저장
'''


@app.route("/saveImagenetFeature", methods=['POST'])
def saveImagenetFeature():

    data = request.get_json()
    img_url = data["img_url"]

    # 이미지 URL을 활용하여 이미지 전처리 후 로드
    img = load_img(img_url)

    # 이미지 feature vector 계산
    features = module(img)

    # 배열에서 single-dimensional entries 제거
    # type : numpy.ndarray, float32, 2048 columns
    feature_vector = np.squeeze(features)

    # Update ElasticSearch - ES의 'img_list' 에서 'item_id' 에 해당하는 이미지 정보에 'imagenet_feature' 넣어주기
    source_to_update = {
        "doc": {
            "imagenet_feature": feature_vector.tolist()
        }
    }

    response = es.update(index="img_list", doc_type="_doc", id=data["item_id"], body=source_to_update)

    return response['result']


'''
이미지 TF 특징점 + 일반 정보 저장

이미지 URL 을 사용하여 이미지의 TF 특징점을 뽑고, 이미지의 일반 정보(item_id, item_name, category_id, img_url)와 함께 ES의 "tf_img_list"에 저장
'''


@app.route("/saveAllImageInfo", methods=['POST'])
def saveAllImageInfo():

    data = request.get_json()

    item_id = data["item_id"]
    item_name = data["item_name"]
    img_url = data["img_url"]

    # 저장 시간
    register_time = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time()))

    # 이미지 URL을 활용하여 이미지 전처리 후 로드
    img = load_img(img_url)

    # 이미지 feature vector 계산
    features = module(img)

    # 배열에서 single-dimensional entries 제거
    # type : numpy.ndarray, float32, 2048 columns
    feature_vector = np.squeeze(features)

    # Save in ElasticSearch - 'tf_img_list' index 에 저장
    imagenet_feature = {"item_id": item_id,
                        "item_name": item_name,
                        "img_url": img_url,
                        "register_time": register_time,
                        "category_id": data["category_id"],
                        "imagenet_feature": feature_vector.tolist()}

    response = es.index(index="tf_img_list", id=item_id, body=imagenet_feature)

    return response['result']


'''
이미지 TF 특징점 추출 

이미지 URL을 사용하여 이미지의 TF 특징점을 뽑고 반환
'''


@app.route("/getImagenetFeature")
def getImagenetFeature():

    parameter_dict = request.args.to_dict()
    img_url = parameter_dict["img_url"]

    # 이미지 URL을 활용하여 이미지 전처리 후 로드
    img = load_img(img_url)

    # 이미지 feature vector 계산
    features = module(img)

    # 배열에서 single-dimensional entries 제거
    # type : numpy.ndarray, float32, 2048 columns
    feature_vector = np.squeeze(features)

    # json
    imagenet_feature = {"imagenet_feature": feature_vector.tolist()}

    return jsonify(imagenet_feature)


if __name__ == "__main__":

    # ElasticSearch 접속
    es = Elasticsearch(es_address)

    # tfhub.dev handle 사용 모듈 정의
    # https://tfhub.dev/google/imagenet/inception_v3/feature_vector/4 에서 Download Model
    module_handle = "model/inception_v3/"

    # 아래 방식은 환경이 달라지면 에러를 유발하므로 지양
    # module_handle = "https://tfhub.dev/google/imagenet/inception_v3/feature_vector/4"

    # 모듈 로드
    module = hub.load(module_handle)

    # run Server
    app.run(host=flask_host, port=flask_port, debug=True)
