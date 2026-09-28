import math
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import Dict, Any, List, Tuple


def count_divisors(n: int) -> int:
    """
    Computes the total number of divisors of an integer n using an O(sqrt(n)) algorithm.

    """
    count = 0
    limit = math.isqrt(n)
    for i in range(1, limit + 1):
        if n % i == 0:
            if i * i == n:
                count += 1
            else:
                count += 2
    return count


def compute_range_divisors(start: int, end: int) -> int:
    """
    Computes the sum of divisor counts for all numbers in [start, end).
    """
    total = 0
    for n in range(start, end):
        total += count_divisors(n)
    return total


def run_sequential(
    start_num: int = 10_000_000,
    total_elements: int = 100_000
) -> Dict[str, Any]:
    """
    Executes sequential computation from start_num to start_num + total_elements.
    Defaults to 10^7 (10,000,000) to 10^7 + 100,000 (10,100,000).
    """
    start_time = time.perf_counter()
    end_num = start_num + total_elements
    total_divisors = compute_range_divisors(start_num, end_num)
    execution_time = time.perf_counter() - start_time

    return {
        "mode": "sequential",
        "start_num": start_num,
        "total_elements": total_elements,
        "total_divisors": total_divisors,
        "execution_time_seconds": round(execution_time, 6),
    }


def run_concurrent(
    start_num: int = 10_000_000,
    total_elements: int = 100_000,
    num_threads: int = 4
) -> Dict[str, Any]:
    """
    Executes the computation by explicitly splitting [start_num, start_num + total_elements)
    across num_threads (default: 4 threads).
    Defaults to 10^7 (10,000,000) to 10^7 + 100,000 (10,100,000).
    """
    start_time = time.perf_counter()
    chunk_size = total_elements // num_threads
    ranges: List[Tuple[int, int]] = []

    for i in range(num_threads):
        c_start = start_num + (i * chunk_size)
        c_end = start_num + total_elements if i == num_threads - 1 else c_start + chunk_size
        ranges.append((c_start, c_end))

    total_divisors = 0

    with ThreadPoolExecutor(max_workers=num_threads) as executor:
        futures = [
            executor.submit(compute_range_divisors, r_start, r_end)
            for r_start, r_end in ranges
        ]
        
        for future in as_completed(futures):
            total_divisors += future.result()

    execution_time = time.perf_counter() - start_time

    return {
        "mode": "concurrent",
        "start_num": start_num,
        "total_elements": total_elements,
        "threads_used": num_threads,
        "total_divisors": total_divisors,
        "execution_time_seconds": round(execution_time, 6),
    }
