export interface ImageRes {

    itemId: string;
    itemName: string;
    categoryId: string;
    imgUrl: string;
    registerTime: string;
    dominantColorHexCandidate: string;

}

export const createImageRes = (): ImageRes => ({

    itemId: "", itemName: "", categoryId: "",
    imgUrl: "", registerTime: "", dominantColorHexCandidate: ""

});

export const setImageRes = (res : any):  ImageRes => ({

    itemId: res.itemId,
    itemName: res.itemName,
    categoryId: res.categoryId,
    imgUrl: res.imgUrl,
    registerTime: res.registerTime,
    dominantColorHexCandidate: res.dominantColorHexCandidate

});