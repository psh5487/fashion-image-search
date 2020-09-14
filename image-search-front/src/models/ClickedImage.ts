import {createImage, Image} from "./Image";

export interface ClickedImage {

    image : Image;
    dominantColorHexCandidate: string;

}

export const createClickedImage = (): ClickedImage => ({

    image : createImage(),
    dominantColorHexCandidate : ""

});

export const createClickedImageAllArgs = (image : Image, dominantColorHexCandidate : string):  ClickedImage => ({

    image : image,
    dominantColorHexCandidate: dominantColorHexCandidate

});