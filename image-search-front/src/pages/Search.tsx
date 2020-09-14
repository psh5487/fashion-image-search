import React from 'react';

import '../App.css';
import './Search.css';
import SearchContent from "../components/SearchContent";
import Header from "../components/Header";

const Search: React.FC = () => {

    return (
        <section id="search">
            <Header/>
            <SearchContent/>
        </section>
    );

}

export default Search;