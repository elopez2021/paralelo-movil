import os
from fastapi import FastAPI
from pydantic import BaseModel, Field
from starlette.concurrency import run_in_threadpool

from services import run_sequential, run_concurrent

app = FastAPI(
    title="Algorithms - Divisor Counting",
    description=(
        "API to compare sequential vs concurrent execution performance "
        "when counting divisors across the base range of 10,000,000 to 10,100,000."
    ),
    version="1.0.0",
)


class SequentialResponse(BaseModel):
    mode: str = Field(..., examples=["sequential"])
    start_num: int = Field(..., examples=[10000000])
    total_elements: int = Field(..., examples=[100000])
    total_divisors: int = Field(..., examples=[1725690])
    execution_time_seconds: float = Field(..., examples=[18.123456])


class ConcurrentResponse(BaseModel):
    mode: str = Field(..., examples=["concurrent"])
    start_num: int = Field(..., examples=[10000000])
    total_elements: int = Field(..., examples=[100000])
    threads_used: int = Field(..., examples=[4])
    total_divisors: int = Field(..., examples=[1725690])
    execution_time_seconds: float = Field(..., examples=[17.589123])


@app.get("/", tags=["Info"])
def read_root():
    return {
        "message": "Concurrent and Sequential Processing Comparison API",
        "endpoints": {
            "sequential": "/api/proceso/secuencial",
            "concurrent": "/api/proceso/concurrente",
            "docs": "/docs",
        },
    }


@app.get("/health", tags=["Info"])
def health_check():
    return {"status": "healthy"}


@app.get(
    "/api/proceso/secuencial",
    response_model=SequentialResponse,
    summary="Sequential divisor computation",
)
async def proceso_secuencial():
    """
    Executes divisor calculation for the range [10,000,000, 10,100,000) sequentially.
    
    """
    result = await run_in_threadpool(run_sequential)
    return result


@app.get(
    "/api/proceso/concurrente",
    response_model=ConcurrentResponse,
    summary="Concurrent divisor computation with worker threads",
)
async def proceso_concurrente():
    """
    Executes divisor calculation for the range [10,000,000, 10,100,000)
    partitioned across 4 worker threads.
      
    """
    result = await run_in_threadpool(run_concurrent)
    return result


if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", 8000))
    uvicorn.run("main:app", host="127.0.0.1", port=port, reload=True)
