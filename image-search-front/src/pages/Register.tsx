import React, { useState } from 'react';
import { useForm } from 'react-hook-form'
import axios from 'axios';
import { Row, Col} from "antd";
import { ClipLoader } from "react-spinners";
import '../App.css';
import './Register.css';

// @ts-ignore
const host = 'http://localhost:8080';

const Register: React.FC = () => {

    const { register, handleSubmit } = useForm();

    const [step, setStep] = useState(0);
    const [imageUrl, setImageUrl] = useState("https://fistiktekstil.com/blog/wp-content/uploads/2019/12/img-700x465.jpg")

    const onChangeImageUrlHandler = (e : any) => {
        setImageUrl(e.target.value);
    }


    const onSubmit = (formData : any) => { registerImage(formData); setStep(step + 1) };

    const registerImage = (formData : any) => {
        axios.post(host + '/images/register', formData)
            .then(function (response) {
                console.log(response);
                setTimeout(() => {
                    setStep(2);
                }, 2000);
            })
            .catch(function (error) {
                console.log(error);
            });
    }

    const registerTitle =

        <Row className='register-row'>
            <Col span={12} offset={6}>
                <h1>이미지 등록</h1>
            </Col>
        </Row>

    const registerForm =

        <Row className='register-row'>
            <Col span={12}>
                <Row>
                    <Col offset = {10} span={8}>
                        <img
                             src={imageUrl}></img>
                    </Col>
                </Row>
            </Col>
            <Col span = {12}>
                <form onSubmit = {handleSubmit(onSubmit)}>
                    <Row className = 'register-input-row'>
                        <Col offset = {0} span={16}>
                            <input name = "itemId" ref={register} placeholder="상품 id"/>
                        </Col>
                    </Row>
                    <Row className = 'register-input-row'>
                        <Col offset = {0} span={16}>
                            <input name = "itemName" ref={register} placeholder="상품 이름"/>
                        </Col>
                    </Row>
                    <Row className = 'register-input-row'>
                        <Col offset = {0} span={16}>
                            <input name = "imageUrl" ref={register}
                                   placeholder="이미지 URL"
                                   onChange = {onChangeImageUrlHandler}
                            />
                        </Col>
                    </Row>
                    <Row className = 'register-input-row'>
                        <Col offset = {0} span={16}>
                            <input type="submit" value="저장" />
                        </Col>
                    </Row>
                </form>
            </Col>
        </Row>

    const submitSpin =
        <Row>
            <Col span={2} offset={11}>
                <ClipLoader
                    size={"15vh"}
                    //size={"150px"} this also works
                    color={"#000000"}
                    loading={true}
                />
            </Col>
        </Row>

    return (
        <section id="register">
            
            <div id = "register-content-wrapper">
                
            {registerTitle}

            { step == 0 ? registerForm :
                step == 1 ? submitSpin :
                     <h1>완료</h1> }
                     
            </div>
            
        </section>
    );
}

export default Register;