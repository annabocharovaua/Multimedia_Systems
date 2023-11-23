import numpy as np


# матриця Хаара є базисними функціями для простору розмірності size x size.
# Ці базисні функції широко використовуються в галузі обробки сигналів та зображень.
# Кожен рядок матриці Хаара є базисною функцією, яка може бути використана для аналізу
# або подання сигналу або зображення.
# Комбінування різних базисних функцій цієї матриці дозволяє аналізувати різні частотні компоненти вхідного сигналу.

def h1(size: int) -> np.ndarray:
    h = np.zeros((size, size))
    for i in range(0, size, 2):
        h[i, i // 2] = 0.5
        h[i + 1, i // 2] = 0.5
    for i in range(size - 1, -1, -2):
        h[i, size - (size - i + 1) // 2] = -0.5
        h[i - 1, size - (size - i + 1) // 2] = 0.5
    return h


def h2(size: int) -> np.ndarray:
    h = np.zeros((size, size))
    h[:size // 2, :size // 2] = h1(size // 2)
    h[size // 2:, size // 2:] = np.eye(size // 2)
    return h


def h3(size: int) -> np.ndarray:
    h = np.eye(size)
    h[0, 0] = h[0, 1] = h[1, 0] = 0.5
    h[1, 1] = -0.5
    return h


def haar_matrix(size: int) -> np.ndarray:
    h1_component = h1(size)
    h2_component = h2(size)
    h3_component = h3(size)
    return np.matmul(np.matmul(h1_component, h2_component), h3_component)


def compress(image: np.ndarray, compression_ratio: float = 1.5) -> np.ndarray:
    validate_image_size(image)

    size = image.shape[0]
    validate_power_of_two(size)

    h_matrix = haar_matrix(size)
    h_transpose = np.transpose(h_matrix)

    b_matrix = np.matmul(np.matmul(h_transpose, image), h_matrix)
    # Обчислення кількості ненульових коефіцієнтів
    non_zero_count = round(calculate_non_zero_count(b_matrix, compression_ratio))
    # Обчислення кількості коефіцієнтів, які будуть встановлені в нуль
    zero_count = b_matrix.size - non_zero_count

    # перетворює матрицю в одномірний масив
    flattened_image = b_matrix.flatten()
    sorted_indices = np.argsort(flattened_image)
    # Встановлення в нуль менш значних коефіцієнтів
    flattened_image[sorted_indices[:zero_count]] = 0

    compressed_image = flattened_image.reshape(b_matrix.shape)
    return compressed_image


def validate_image_size(image: np.ndarray):
    if image.shape[0] != image.shape[1]:
        raise ValueError("Input image must be square.")


def validate_power_of_two(size: int):
    if not ((size & (size - 1) == 0) and size != 0):
        raise ValueError("The size of the input image must be a power of two.")

#Більше значення compression_ratio призведе до більшої кількості нульових коефіцієнтів і, отже, більшого стиснення.
def calculate_non_zero_count(image: np.ndarray, compression_ratio: float) -> float:
    return np.sum(np.abs(image) > 1e-10) / compression_ratio


def decompress(encoded_image: np.ndarray) -> np.ndarray:
    size = encoded_image.shape[0]
    h_matrix = haar_matrix(size)
    h_transpose = np.transpose(h_matrix)

    decompressed_image = np.matmul(np.matmul(np.linalg.inv(h_transpose), encoded_image), np.linalg.inv(h_matrix))
    decompressed_image = clip_and_round(decompressed_image)

    return decompressed_image.astype(np.uint8)


def clip_and_round(image: np.ndarray) -> np.ndarray:
    clipped_image = np.clip(np.round(image), 0, 255)
    return clipped_image
