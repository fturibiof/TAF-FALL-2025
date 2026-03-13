// Usage: mongosh ... import_mock_data.js -- --wipe
// import_mock_data.js (runs inside mongosh)

const fs = require("fs");

// ✅ WIPE flag comes from environment variable (works even on older mongosh)
const WIPE = (typeof process !== "undefined" && process.env && process.env.WIPE === "1");

const FILE = "/tmp/mockData.json";
if (!fs.existsSync(FILE)) {
  print(`ERROR: ${FILE} not found inside container.`);
  quit(1);
}

const data = JSON.parse(fs.readFileSync(FILE, "utf8"));
const runs = data?.collections?.test_runs || [];
const cases = data?.collections?.test_cases || [];

print(`Loaded mockData.json -> runs=${runs.length}, cases=${cases.length}`);

const runIds = runs.map(r => r?.pipeline?.runId).filter(Boolean);

if (WIPE) {
  print("WIPE enabled: deleting previous docs for these runIds...");
  const delCases = db.test_cases.deleteMany({ runId: { $in: runIds } });
  print(`Deleted test_cases: ${delCases.deletedCount}`);

  const delRuns = db.test_runs.deleteMany({ "pipeline.runId": { $in: runIds } });
  print(`Deleted test_runs: ${delRuns.deletedCount}`);
}

if (runs.length) {
  const resRuns = db.test_runs.insertMany(runs, { ordered: false });
  print(`Inserted test_runs: ${Object.keys(resRuns.insertedIds).length}`);
}

if (cases.length) {
  const resCases = db.test_cases.insertMany(cases, { ordered: false });
  print(`Inserted test_cases: ${Object.keys(resCases.insertedIds).length}`);
}

print("Done.");