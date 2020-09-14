export interface SearchImageRes {

    itemId: string;
    itemName: string;
    imgUrl : string;
    registerTime: string;
    score : number;
    dominantColorHex: string;
    dominantColorHexCandidate: string;
    dominantColorHSV: string;

}

export const createSearchImageRes = (): SearchImageRes => ({

    itemId: "", itemName: "", imgUrl: "", registerTime: "",
    score: 0.0, dominantColorHex: "", dominantColorHexCandidate: "", dominantColorHSV: ""

});

export const setSearchImageRes = (res : any):  SearchImageRes => ({

    itemId: res.itemId,
    itemName: res.itemName,
    imgUrl: res.imgUrl,
    registerTime: res.registerTime,
    score: res.score,
    dominantColorHex: res.dominantColorHex,
    dominantColorHexCandidate: res.dominantColorHexCandidate,
    dominantColorHSV: res.dominantColrHSV

});