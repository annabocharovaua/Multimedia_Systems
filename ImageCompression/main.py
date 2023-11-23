import math

import cv2
import numpy as np

import dct_algorithm
import haar_algorithm

def calculate_psnr(original_image, reconstructed_image):
    mse = np.mean((original_image - reconstructed_image) ** 2)
    if mse == 0:
        return 100
    max_pixel = 255.0
    psnr_value = 20 * math.log10(max_pixel / math.sqrt(mse))
    return psnr_value

def main():
    input_path = input("Введіть шлях до зображення: ")
    input_image = cv2.imread(input_path, 0)

    cv2.imshow("Original Image", input_image)

    method = input("Оберіть метод (dct/haar): ")

    if method == 'dct':
        quality = int(input("Введіть значення якості DCT (ціле число від 1 до 100): "))
        compressed_image = dct_algorithm.compress_image(input_image, quality)
        decompressed_image = dct_algorithm.decompress_image(compressed_image)
        print(f"{input_path} --> DCT")
    elif method == 'haar':
        compression_ratio = float(input("Введіть коефіціенти стиснення для Wavelet Haar (дробове число): "))
        compressed_image = haar_algorithm.compress(input_image, compression_ratio)
        decompressed_image = haar_algorithm.decompress(compressed_image)
        print(f"{input_path} --> Haar")
    else:
        print("Некоректний метод. Введіть 'dct' или 'haar'")
        return

    print("psnr = ", calculate_psnr(input_image, decompressed_image))

    cv2.imshow("Decompressed Image", decompressed_image)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

if __name__ == '__main__':
    main()
