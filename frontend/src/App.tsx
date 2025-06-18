import { useState } from "react";

function App() {
  const [count, setCount] = useState(0);

  return (
    <div className="min-h-screen bg-gray-100 text-gray-900 flex flex-col items-center justify-center p-8">
      <h1 className="text-4xl font-bold text-blue-600 mb-4">Hello Tailwind!</h1>

      <h2 className="text-2xl text-blue-500 mb-6">World with Tailwind CSS</h2>

      <div className="bg-white shadow-md rounded-xl p-6 w-full max-w-sm text-center">
        <p className="text-lg font-medium mb-4">Current count:</p>
        <p className="text-2xl font-bold text-indigo-600 mb-4">{count}</p>

        <button
          className="px-4 py-2 bg-indigo-500 text-white font-semibold rounded-lg shadow hover:bg-indigo-600 transition"
          onClick={() => setCount((c) => c + 1)}
        >
          Increment
        </button>
      </div>
    </div>
  );
}

export default App;
