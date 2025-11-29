<?php
namespace Tests;

use PHPUnit\Framework\TestCase;
use App\Calculator;

class CalculatorTest extends TestCase
{
    public function testAddition()
    {
        $calculator = new Calculator();
        $this->assertEquals(5, $calculator->add(2, 3));
    }

    public function testSubtraction()
    {
        $calculator = new Calculator();
        $this->assertEquals(1, $calculator->subtract(4, 3));
    }
}