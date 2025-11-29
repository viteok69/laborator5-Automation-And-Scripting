<?php
require 'vendor/autoload.php';
use App\Calculator;

$calculator = new Calculator();
$result = $calculator->add(10, 5);

echo "<h1>Proiect CI/CD Lab 05</h1>";
echo "<p>Rezultatul adunării (10 + 5) este: " . $result . "</p>";
echo "<p>Versiunea PHP: " . phpversion() . "</p>";