import React from 'react';
import { Row, Col } from "antd";
import './Header.css';

interface HeaderProps {
}

const Header: React.FC<HeaderProps> = () => {

    return (
        <div>
            <Row id="header-row">
                <Col span={8} offset={8}>
                    <span className = "header-text">유사 이미지 검색 서비스</span>
                </Col>
            </Row>
        </div>
    );
}

export default Header;