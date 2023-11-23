import numpy as np

size: int
Q50_values: np.ndarray
T: np.ndarray


# Для рівня якості та стиснення ми використовуємо матрицю квантування Q50.
# Для рівня якості вище 50 (менший стиск, більше висока якість зображення)
# стандартна матриця квантування множиться на (100 мінус рівень Якості) / 50.
# Для рівня якості менше 50 (більше стиснення, нижча якість зображення)
# стандартна матриця квантування множиться на 50/рівень якості.
def generate_quantization_matrix(quality_level: int) -> np.ndarray:
    if not (1 <= quality_level <= 100):
        raise ValueError("Quality level should be in the range from 1 to 100, inclusive.")
    # Вибір матриці квантування в залежності від рівня якості
    if quality_level == 50:
        Q = Q50_values
    elif quality_level > 50:
        Q = Q50_values * ((100 - quality_level) / 50)
    else:
       Q = Q50_values * (50 / quality_level)

    # Округлення та обрізка значень матриці квантування
    Q = np.clip(np.round(Q), 1, 255).astype(int)

    return Q


# Цикли за діагоналями:
# Для кожної діагоналі (пари (i, j) у циклі), де i - номер рядка, і j - номер стовпця:
# Якщо номер діагоналі парний, додаємо елемент matrix [i - j, j] до списку.
# Якщо номер діагоналі непарний, додаємо елемент matrix [j, i - j] до списку.
def zigzag_encode(matrix: np.ndarray) -> list:
    zigzag_list = []

    for i in range(2 * size - 1):
        if i % 2 == 0:
            start_j = max(0, i - size + 1)
            end_j = min(i, size - 1) + 1
            for j in range(start_j, end_j):
                zigzag_list.append(matrix[i - j, j])
        else:
            start_j = max(0, i - size + 1)
            end_j = min(i, size - 1) + 1
            for j in range(start_j, end_j):
                zigzag_list.append(matrix[j, i - j])

    return zigzag_list


def zigzag_decode(zigzag_list: list) -> np.ndarray:
    matrix = np.zeros((size, size))
    count = 0

    for i in range(2 * size - 1):
        start_j = max(0, i - size + 1)
        end_j = min(i, size - 1) + 1
        step = 1 if i % 2 == 0 else 1

        for j in range(start_j, end_j, step):
            if i % 2 == 0:
                matrix[i - j, j] = zigzag_list[count]
            else:
                matrix[j, i - j] = zigzag_list[count]
            count += 1

    return matrix


# Run-Length Encoding (RLE) - це метод стиснення даних, який замінює послідовності однакових значень,
# що повторюються, короткими вказівниками на значення та їх кількість.
# input_data = [2, 2, 2, 3, 3, 1, 4, 4, 4, 4, 5, 6, 6, 6]
# [(2, 3), (3, 2), (1, 1), (4, 4), (5, 1), (6, 3)]
def rle_func(data: list) -> list:
    result = []
    current = data[0]
    count = 1

    for value in data[1:]:
        if value == current:
            count += 1
        else:
            result.append((current, count) if count > 1 else (current,))
            current = value
            count = 1

    result.append((current, count) if count > 1 else (current,))
    return result


def decode_rle(encoded_data):
    decoded_values = []
    for elem in encoded_data:
        if len(elem) == 1:
            decoded_values.append(elem[0])
        else:
            value, repeat_count = elem
            decoded_values.extend([value] * repeat_count)
    return decoded_values


def compress_image(image: np.ndarray, quality_level: int = 50) -> list:
    if image.shape[0] % size != 0 or image.shape[1] % size != 0:
        raise ValueError(f"The image dimensions must be divisible by the specified block size.")

    Q = generate_quantization_matrix(quality_level)

    encoded_image = [quality_level]

    for block_i in range(0, image.shape[0], size):
        encoded_row = []
        for block_j in range(0, image.shape[1], size):
            block = image[block_i:block_i + size, block_j:block_j + size].astype(int)
            ##Оскільки DCT призначений для роботи зі значеннями пікселів в діапазоні від -128 до 127,
            # вихідний блок «вирівнюється» шляхом віднімання 128 з кожного запису.
            M = block - 128
            # Матриця перетвореного зображення TMT
            D = np.matmul(np.matmul(T, M), np.transpose(T))
            ## Квантування досягається шляхом поділу кожного елемента у матриці перетвореного зображення
            # D на відповідний елемент у матриці квантування Q та подальшого округлення до найближчого
            # цілого значення. Коефіцієнти, розташовані у верхньому лівому кутку, відповідають нижнім
            # частотатом блоку зображення, до яких людське око найбільш чутливе. Крім того, нулі
            # являють собою менш важливі, вищі частоти, які були відкинуті, що призвело до втрат
            C = np.round(D / Q).astype(int)
            ##Після квантування більшість коефіцієнтів зазвичай рівні нулю. JPEG використовує цю перевагу, кодуючи квантовані коефіцієнти
            # зигзагоподібної послідовності, показаної на малюнку 1. Перевага полягає в
            # консолідації щодо великих серій нулів, які дуже добре стискаються.
            encoded_block = rle_func(zigzag_encode(C))
            encoded_row.append(encoded_block)
        encoded_image.append(encoded_row)

    return encoded_image


def decompress_image(encoded_image: list) -> np.ndarray:
    quality_level, *encoded_rows = encoded_image
    Q = generate_quantization_matrix(quality_level)
    image = np.zeros((len(encoded_rows) * size,) * 2, dtype=np.uint8)
    for encoded_row, block_i in zip(encoded_rows, range(0, image.shape[0], size)):
        for encoded_block, block_j in zip(encoded_row, range(0, image.shape[1], size)):
            # Реконструкція нашого зображення починається з декодування потоку бітів, що представляє
            # квантованную матрицу C. Потім кожен елемент C множиться на відповідний елемент
            # первоначально використаної матриці квантування. Ri,j = Qij x Cij
            C = zigzag_decode(decode_rle(encoded_block))
            R = Q * C
            N = np.clip(np.round(np.matmul(np.matmul(np.transpose(T), R), T)) + 128, 0,
                        255)
            image[block_i:block_i + size, block_j:block_j + size] = N
    return image


def initialize():
    global size, Q50_values, T

    size = 8
    Q50_values = np.array([[16, 11, 10, 16, 24, 40, 51, 61],
                           [12, 12, 14, 19, 26, 58, 60, 55],
                           [14, 13, 16, 24, 40, 57, 69, 56],
                           [14, 17, 22, 29, 51, 87, 80, 62],
                           [18, 22, 37, 56, 68, 109, 103, 77],
                           [24, 35, 55, 64, 81, 104, 113, 92],
                           [49, 64, 78, 87, 103, 121, 120, 101],
                           [72, 92, 95, 98, 112, 100, 103, 99]])

    a = 1 / np.sqrt(size)
    b = np.sqrt(2 / size)
    T = np.array(
        [[a if i == 0 else b * np.cos((2 * j + 1) * i * np.pi / (2 * size)) for j in range(size)]
         for i in range(size)])


initialize()
