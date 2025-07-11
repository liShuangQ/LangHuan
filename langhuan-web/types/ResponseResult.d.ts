/* eslint-disable no-unused-vars */
interface ResponseResult<T> {
  code: number;
  success: boolean;
  message: string;
  data: T;
}
