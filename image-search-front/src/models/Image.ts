export interface Image {

    itemId: string;
    itemName: string;
    categoryId: string;
    imgUrl: string;

}

export const createImage = (): Image => ({

    itemId: "",
    itemName: "",
    categoryId: "",
    imgUrl: ""

});

export const setImage = (itemId : string, itemName : string,
                         categoryId : string, imgUrl : string):  Image => ({

    itemId: itemId,
    itemName: itemName,
    categoryId: categoryId,
    imgUrl: imgUrl

});