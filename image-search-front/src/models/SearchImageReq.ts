import {Image} from "./Image";

export interface SearchImageReq {

    searchType : string;
    scoreRatioArr : number[];
    image : Image;

}

export const createSearchImageReqAllArgs = (searchType: string, scoreRatioArr: number[], image: Image):  SearchImageReq => ({

    searchType : searchType,
    scoreRatioArr : scoreRatioArr,
    image : image

});