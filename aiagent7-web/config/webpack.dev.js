/* eslint-disable @typescript-eslint/no-var-requires */
const baseConfig = require( "./webpack.base.js" );
const { merge } = require( "webpack-merge" );
const FriendlyErrorsWebpackPlugin = require('@soda/friendly-errors-webpack-plugin');
const ESLintPlugin = require( "eslint-webpack-plugin" );
module.exports = merge( baseConfig, {
    mode: "development",
    target: "web",
    devServer: {
        port: '8077',
        hot: true,
        open: false,
        client: {
            overlay: false,
        },
        proxy: {
            // 配置代理，解决开发环境的跨域问题
            // '/api': 'http://localhost:3000'
        },
    },
    devtool: "eval-cheap-module-source-map",//eval-cheap-module-source-map
    plugins: [
        new FriendlyErrorsWebpackPlugin(),
        new ESLintPlugin( { extensions: [ "js", "ts", "vue" ] } ),
    ],
} );
