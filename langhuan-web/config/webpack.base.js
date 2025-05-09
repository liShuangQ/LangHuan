/* eslint-disable @typescript-eslint/no-var-requires */
const devMode = process.env.NODE_ENV !== "production";
const HtmlWebpackPlugin = require("html-webpack-plugin");
const {VueLoaderPlugin} = require("vue-loader");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const path = require("path");
const AutoImport = require('unplugin-auto-import/webpack')
const AutoComponents = require('unplugin-vue-components/webpack')
const {ElementPlusResolver} = require('unplugin-vue-components/resolvers')
const webpack = require("webpack");
const dotenv = require('dotenv');
const DotenvWebpack = require('dotenv-webpack');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const envConfig = dotenv.config({
    path: path.resolve(__dirname, '../.env'),
    encoding: 'utf8',
    debug: false,
}).parsed;
if (!envConfig) {
    console.warn('配置文件不存在');
    process.exit(1);
}
module.exports = {
    entry: "./src/main.ts",
    module: {
        rules: [
            {
                test: /\.vue$/,
                use: "vue-loader",
            },
            {
                test: /\.(t|j)s$/,
                exclude: /node_modules/,
                use: {
                    loader: "babel-loader",
                    options: {
                        cacheDirectory: true,
                    },
                },
            },
            {
                test: /\.(png|svg|jpe?g|gif)$/,
                type: "asset",
                generator: {
                    filename: "images/[name]-[hash][ext]",
                },
            },
            {
                test: /\.(eot|svg|ttf|woff2?|)$/,
                type: "asset/resource",
                generator: {
                    filename: "fonts/[name]-[hash][ext]",
                },
            },
            {
                test: /\.(sa|sc|c)ss$/,
                use: devMode
                    ? ["style-loader", "css-loader", "postcss-loader", {
                        loader: 'sass-loader',
                        options: {
                            sassOptions: {
                                silenceDeprecations: ['legacy-js-api'],
                            },
                        },
                    },]
                    : [
                        MiniCssExtractPlugin.loader,
                        "css-loader",
                        "postcss-loader",
                        {
                            loader: 'sass-loader',
                            options: {
                                sassOptions: {
                                    silenceDeprecations: ['legacy-js-api'],
                                    quietDeps: true, // 抑制弃用警告
                                },
                            },
                        },
                    ],
            },
        ],
    },
    plugins: [
        new webpack.DefinePlugin({
            __VUE_OPTIONS_API__: true,
            __VUE_PROD_DEVTOOLS__: false
        }),
        new HtmlWebpackPlugin({
            title: process.env.BASE_PROJECT_NAME,
            template: "./index.html",
        }),
        new VueLoaderPlugin(),
        AutoImport({
            resolvers: [ElementPlusResolver()],
            imports: ["vue", "vue-router"],
            dts: "types/auto-imports.d.ts",
        }),
        AutoComponents({
            resolvers: [ElementPlusResolver()],
            dts: 'types/components.d.ts'
        }),
        new DotenvWebpack({
            path: path.resolve(__dirname, '../.env'),
        }),
        new CopyWebpackPlugin({
            patterns: [
                {from: "static", to: "static"},
            ],
        }),
    ],
    resolve: {
        extensions: [".js", ".ts", ".tsx", ".json", ".vue"], // 改变引入文件， 可以不写后缀名
        alias: {
            "@": path.resolve(__dirname, "../src"),
        },
    },
    cache: {
        type: "filesystem",
    },
    output: {
        filename: './js/[name].js',
        path: path.resolve(__dirname, '../dist/langhuan'),
        publicPath: devMode ? '/' : '/'
    }
};
