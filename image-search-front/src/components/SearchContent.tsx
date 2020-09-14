import React, {useEffect, useState} from 'react';

import axios from 'axios';
import {Row, Col} from "antd";
import {BeatLoader} from "react-spinners";

// @ts-ignore
import {GridLayout} from '@egjs/react-infinitegrid';

import {ClickedImage, createClickedImage, createClickedImageAllArgs} from "../models/ClickedImage";

import '../App.css';
import '../pages/Search.css';


// @ts-ignore
import {RadioGroup, RadioButton} from 'react-radio-buttons';

import {setImage} from "../models/Image";
import {createSearchImageReqAllArgs} from "../models/SearchImageReq";
import {createImageRes, ImageRes} from "../models/ImageRes";
import {createSearchImageRes, SearchImageRes} from "../models/SearchImageRes";

// @ts-ignore
const host = 'http://localhost:8080';


const SearchContent: React.FC = () => {


    const [clickedItem, setClickedItem] =

        useState<ClickedImage>(
            createClickedImage()
        );


    const [step, setStep] = useState(0);


    const [imageRes, setImageRes] =

        useState<ImageRes[]>([
            createImageRes()]
        );


    const [searchImageRes, setSearchImageRes] =

        useState<SearchImageRes[]>([
            createSearchImageRes()]
        );


    const [searchType, setSearchType] = useState("COLOR");


    const [filterRatio] = useState([0.4, 0.6]);


    useEffect(() => {

        getAllImages();

    }, []);


    useEffect(() => {

        if (step >= 2)
            searchImage(clickedItem, "COLOR");

    }, [clickedItem]);


    useEffect(() => {

        if (step === 2)
            searchImage(clickedItem, searchType);

    }, [searchType]);


    const onClickSearchButtonHandler = (r: any) => {

        setClickedItem(
            createClickedImageAllArgs(setImage(r.itemId, r.itemName, r.categoryId,
                r.imgUrl), r.dominantColorHexCandidate)
        );

        setSearchType("COLOR");

        setStep(step + 1);
    }


    const onClickSearchTypeChangeButtonHandler = (searchType: string) => {

        setStep(4);

        searchImage(clickedItem, searchType);

        setSearchType(searchType);
    }


    const getAllImages = () => {

        axios.get(host + '/images/search/all')

            .then(function (response) {

                const responseData = response.data;

                const limitedResponseData = responseData.slice(0, 100);

                setImageRes(limitedResponseData);

                setStep(step + 1);

            })

            .catch(function (error) {
                console.log(error);
            });

    }

    const searchImage = (clickedItem: any, searchType: string) => {

        axios.post(host + '/images/search',

            createSearchImageReqAllArgs(searchType, filterRatio, clickedItem.image))

            .then(function (response) {

                const responseData = response.data;
                const limitedResponseData = responseData.slice(0, 100);

                setSearchImageRes(limitedResponseData)

                setTimeout(() => {
                    setStep(3);
                }, 100);

            })

            .catch(function (error) {
                console.log(error);
            });

    }

    const searchModeButton =
        <Row className='search-header-row'>
            <Col offset={6} span={12}>
                <Row>
                    <RadioGroup onChange={(value: any) => {
                        onClickSearchTypeChangeButtonHandler(value)
                    }} value={searchType} horizontal>
                        <RadioButton value="COLOR" pointColor="black" rootColor="lightgray">
                            색상
                        </RadioButton>
                        <RadioButton value="PATTERN" pointColor="black" rootColor="lightgray">
                            패턴
                        </RadioButton>
                        <RadioButton value="DESCRIPTOR" pointColor="black" rootColor="lightgray">
                            특징점
                        </RadioButton>
                        <RadioButton value="TF_FEATURES" pointColor="black" rootColor="lightgray">
                            TF 특징점
                        </RadioButton>
                        <RadioButton value="ALL" pointColor="black" rootColor="lightgray">
                            전체
                        </RadioButton>
                    </RadioGroup>
                </Row>
            </Col>
        </Row>


    /*
     *  로딩 Spinner
     */


    const submitSpin =

        <Row style={{marginTop: "20%"}}>
            <Col span={3} offset={11} style={{marginBottom: "5vh"}}>

                <BeatLoader
                    size="40px"
                    color={"#000000"}
                    loading={true}
                />

            </Col>
        </Row>


    /*
     *  검색 결과 리스트 (GridLayout)
     */


    const searchedList =
        <div>
            <GridLayout

                className="gridlayout container"

                options={{
                    isOverflowScroll: false,
                    useFit: true,
                    useRecycle: true,
                    horizontal: false,
                }}>

                <Col style={{cursor: "pointer"}} onClick={() => {
                }} span={8} offset={8}>
                    <Row>
                        <Col offset={0} span={24}>
                            <img style={{border: "none", borderRadius: "10px"}}
                                 src={clickedItem.image.imgUrl}></img>
                        </Col>
                    </Row>
                    <Row className='search-input-row'>
                        <Col offset={0} span={22}>
                            <h4>{clickedItem.image.itemName}</h4>
                        </Col>
                        <Col offset={0} span={2}>
                            <div style={{
                                background: clickedItem.dominantColorHexCandidate,
                                width: "20px", height: "20px", borderRadius: "10px"
                            }}></div>
                        </Col>
                    </Row>
                    <Row>
                        <Col offset={0} span={22}>
                            <h4>{clickedItem.image.itemId}</h4>
                        </Col>
                    </Row>
                </Col>

            </GridLayout>

            <GridLayout

                className="gridlayout container"

                options={{
                    isOverflowScroll: false,
                    useFit: true,
                    useRecycle: true,
                    horizontal: false,
                }}

                layoutOptions={{
                    margin: 5,
                    align: "center"
                }}>

                {searchImageRes.filter((r) => {

                    // 클릭한 상품 고유 번호와 검색 리스트 내 상품 고유 번호간의 중복 filter

                    if (clickedItem.image.itemId !== r.itemId) return true;

                    else return false;

                }).map((r, key) =>

                    <Col key = {key} style={{cursor: "pointer", marginTop: "5vh"}} onClick={() => {
                        onClickSearchButtonHandler(r);
                    }} span={4}>
                        <Row>
                            <Col offset={0} span={24}>
                                <img style={{border: "none", borderRadius: "10px"}}
                                     src={r.imgUrl}></img>
                            </Col>
                        </Row>
                        <Row className='search-input-row'>
                            <Col offset={0} span={4}>
                                <h4 style={{fontWeight: 700}}>유사도</h4>
                            </Col>
                            <Col offset={1} span={19}>
                                <h4 style={{fontWeight: 700, color: "#ff69b4"}}>{100 - Math.round(r.score * 100)}%</h4>
                            </Col>
                        </Row>
                        <Row>
                            <Col offset={0} span={22}>
                                <h4>{r.itemName}</h4>
                            </Col>
                            <Col offset={0} span={2}>
                                <div style={{
                                    background: r.dominantColorHexCandidate,
                                    width: "20px", height: "20px", borderRadius: "10px"
                                }}></div>
                            </Col>
                        </Row>
                        <Row>
                            <Col offset={0} span={22}>
                                <h4>{r.itemId}</h4>
                            </Col>
                        </Row>
                    </Col>
                )}

            </GridLayout>
        </div>


    /*
     *  전체 상품 리스트 (GridLayout)
     */


    const allList =

        <GridLayout

            className="gridlayout container"

            options={{
                isOverflowScroll: false,
                useFit: true,
                useRecycle: true,
                horizontal: false,
            }}

            layoutOptions={{
                margin: 5,
                align: "center",
            }}>

            {imageRes.map((r, key) =>

                <Col key={key} style={{cursor: "pointer"}} onClick={() => {
                    onClickSearchButtonHandler(r)
                }} span={4}>
                    <Row>
                        <Col offset={0} span={24}>
                            <img style={{border: "none", borderRadius: "10px"}}
                                 src={r.imgUrl}></img>
                        </Col>
                    </Row>
                    <Row className='search-input-row'>
                        <Col offset={0} span={22}>
                            <h4>{r.itemName}</h4>
                        </Col>
                        <Col offset={0} span={2}>
                            <div style={{
                                background: r.dominantColorHexCandidate,
                                width: "20px", height: "20px", borderRadius: "10px"
                            }}></div>
                        </Col>
                    </Row>
                    <Row>
                        <Col offset={0} span={22}>
                            <h4>{r.itemId}</h4>
                        </Col>
                    </Row>
                </Col>
            )}

        </GridLayout>


    return (

        <div id={step === 0 ? "search-content-wrapper-initial" : "search-content-wrapper"}>

            {step > 2 ? searchModeButton : null}

            {step === 0 ? submitSpin :
                step === 1 ? allList :
                    step === 2 ? submitSpin :
                        step === 3 ? searchedList :
                            step === 4 ? submitSpin : null}

        </div>

    );
}

export default SearchContent;