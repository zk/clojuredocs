class CCQuickRef
	def self.spheres
		[
			{
				:name => "Simple Values",
				:categories => [
					{
						:name => "Numbers",
						:groups => [
							{
								:name => "Arithmetic",
								:symbols => [
									{
										:name => "+",
										:ns => "clojure.core",
										:link => "/v/1638",
										:id => 1638
									},
									{
										:name => "-",
										:ns => "clojure.core",
										:link => "/v/1659",
										:id => 1659
									},
									{
										:name => "*",
										:ns => "clojure.core",
										:link => "/v/1654",
										:id => 1654
									},
									{
										:name => "/",
										:ns => "clojure.core",
										:link => "/v/1699",
										:id => 1699
									},
									{
										:name => "quot",
										:ns => "clojure.core",
										:link => "/v/1839",
										:id => 1839
									},
									{
										:name => "rem",
										:ns => "clojure.core",
										:link => "/v/1604",
										:id => 1604
									},
									{
										:name => "mod",
										:ns => "clojure.core",
										:link => "/v/2003",
										:id => 2003
									},
									{
										:name => "inc",
										:ns => "clojure.core",
										:link => "/v/1763",
										:id => 1763
									},
									{
										:name => "dec",
										:ns => "clojure.core",
										:link => "/v/2014",
										:id => 2014
									},
									{
										:name => "max",
										:ns => "clojure.core",
										:link => "/v/1903",
										:id => 1903
									},
									{
										:name => "min",
										:ns => "clojure.core",
										:link => "/v/2104",
										:id => 2104
									},
									{
										:name => "with-precision",
										:ns => "clojure.core",
										:link => "/v/1814",
										:id => 1814
									}
								]

							},
							{
								:name => "Compare",
								:symbols => [
									{
										:name => "=",
										:ns => "clojure.core",
										:link => "/v/1885",
										:id => 1885
									},
									{
										:name => "==",
										:ns => "clojure.core",
										:link => "/v/1511",
										:id => 1511
									},
									{
										:name => "not=",
										:ns => "clojure.core",
										:link => "/v/1530",
										:id => 1530
									},
									{
										:name => "<",
										:ns => "clojure.core",
										:link => "/v/1860",
										:id => 1860
									},
									{
										:name => ">",
										:ns => "clojure.core",
										:link => "/v/1902",
										:id => 1902
									},
									{
										:name => "<=",
										:ns => "clojure.core",
										:link => "/v/1942",
										:id => 1942
									},
									{
										:name => ">=",
										:ns => "clojure.core",
										:link => "/v/1922",
										:id => 1922
									}
								]

							},
							{
								:name => "Bitwise Operations",
								:symbols => [
									{
										:name => "bit-and",
										:ns => "clojure.core",
										:link => "/v/1675",
										:id => 1675
									},
									{
										:name => "bit-or",
										:ns => "clojure.core",
										:link => "/v/1535",
										:id => 1535
									},
									{
										:name => "bit-xor",
										:ns => "clojure.core",
										:link => "/v/1582",
										:id => 1582
									},
									{
										:name => "bit-flip",
										:ns => "clojure.core",
										:link => "/v/1673",
										:id => 1673
									},
									{
										:name => "bit-not",
										:ns => "clojure.core",
										:link => "/v/1774",
										:id => 1774
									},
									{
										:name => "bit-clear",
										:ns => "clojure.core",
										:link => "/v/1819",
										:id => 1819
									},
									{
										:name => "bit-set",
										:ns => "clojure.core",
										:link => "/v/1554",
										:id => 1554
									},
									{
										:name => "bit-shift-left",
										:ns => "clojure.core",
										:link => "/v/1929",
										:id => 1929
									},
									{
										:name => "bit-shift-right",
										:ns => "clojure.core",
										:link => "/v/2030",
										:id => 2030
									},
									{
										:name => "bit-test",
										:ns => "clojure.core",
										:link => "/v/2106",
										:id => 2106
									}
								]

							},
							{
								:name => "Cast",
								:symbols => [
									{
										:name => "byte",
										:ns => "clojure.core",
										:link => "/v/1852",
										:id => 1852
									},
									{
										:name => "short",
										:ns => "clojure.core",
										:link => "/v/1576",
										:id => 1576
									},
									{
										:name => "int",
										:ns => "clojure.core",
										:link => "/v/2001",
										:id => 2001
									},
									{
										:name => "long",
										:ns => "clojure.core",
										:link => "/v/1566",
										:id => 1566
									},
									{
										:name => "float",
										:ns => "clojure.core",
										:link => "/v/1731",
										:id => 1731
									},
									{
										:name => "double",
										:ns => "clojure.core",
										:link => "/v/2043",
										:id => 2043
									},
									{
										:name => "bigint",
										:ns => "clojure.core",
										:link => "/v/2013",
										:id => 2013
									},
									{
										:name => "bigdec",
										:ns => "clojure.core",
										:link => "/v/2004",
										:id => 2004
									},
									{
										:name => "num",
										:ns => "clojure.core",
										:link => "/v/2034",
										:id => 2034
									},
									{
										:name => "rationalize",
										:ns => "clojure.core",
										:link => "/v/1861",
										:id => 1861
									}
								]

							},
							{
								:name => "Test",
								:symbols => [
									{
										:name => "nil?",
										:ns => "clojure.core",
										:link => "/v/1542",
										:id => 1542
									},
									{
										:name => "identical?",
										:ns => "clojure.core",
										:link => "/v/1666",
										:id => 1666
									},
									{
										:name => "zero?",
										:ns => "clojure.core",
										:link => "/v/1674",
										:id => 1674
									},
									{
										:name => "pos?",
										:ns => "clojure.core",
										:link => "/v/1955",
										:id => 1955
									},
									{
										:name => "neg?",
										:ns => "clojure.core",
										:link => "/v/1708",
										:id => 1708
									},
									{
										:name => "even?",
										:ns => "clojure.core",
										:link => "/v/1927",
										:id => 1927
									},
									{
										:name => "odd?",
										:ns => "clojure.core",
										:link => "/v/1605",
										:id => 1605
									}
								]

							}
						]

					},
					{
						:name => "Symbols / Keywords",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "keyword",
										:ns => "clojure.core",
										:link => "/v/1995",
										:id => 1995
									},
									{
										:name => "symbol",
										:ns => "clojure.core",
										:link => "/v/1624",
										:id => 1624
									}
								]

							},
							{
								:name => "Use",
								:symbols => [
									{
										:name => "name",
										:ns => "clojure.core",
										:link => "/v/1800",
										:id => 1800
									},
									{
										:name => "intern",
										:ns => "clojure.core",
										:link => "/v/1779",
										:id => 1779
									},
									{
										:name => "namespace",
										:ns => "clojure.core",
										:link => "/v/1858",
										:id => 1858
									}
								]

							},
							{
								:name => "Test",
								:symbols => [
									{
										:name => "keyword?",
										:ns => "clojure.core",
										:link => "/v/1497",
										:id => 1497
									},
									{
										:name => "symbol?",
										:ns => "clojure.core",
										:link => "/v/1606",
										:id => 1606
									}
								]

							}
						]

					},
					{
						:name => "Strings / Characters",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "str",
										:ns => "clojure.core",
										:link => "/v/1878",
										:id => 1878
									},
									{
										:name => "print-str",
										:ns => "clojure.core",
										:link => "/v/1716",
										:id => 1716
									},
									{
										:name => "println-str",
										:ns => "clojure.core",
										:link => "/v/2081",
										:id => 2081
									},
									{
										:name => "pr-str",
										:ns => "clojure.core",
										:link => "/v/1859",
										:id => 1859
									},
									{
										:name => "prn-str",
										:ns => "clojure.core",
										:link => "/v/1748",
										:id => 1748
									},
									{
										:name => "with-out-str",
										:ns => "clojure.core",
										:link => "/v/1652",
										:id => 1652
									}
								]

							},
							{
								:name => "Use",
								:symbols => [
									{
										:name => "count",
										:ns => "clojure.core",
										:link => "/v/1846",
										:id => 1846
									},
									{
										:name => "get",
										:ns => "clojure.core",
										:link => "/v/1941",
										:id => 1941
									},
									{
										:name => "subs",
										:ns => "clojure.core",
										:link => "/v/1913",
										:id => 1913
									},
									{
										:name => "format",
										:ns => "clojure.core",
										:link => "/v/1521",
										:id => 1521
									}
								]

							},
							{
								:name => "Cast / Test",
								:symbols => [
									{
										:name => "char",
										:ns => "clojure.core",
										:link => "/v/1957",
										:id => 1957
									},
									{
										:name => "char?",
										:ns => "clojure.core",
										:link => "/v/1684",
										:id => 1684
									},
									{
										:name => "string?",
										:ns => "clojure.core",
										:link => "/v/1543",
										:id => 1543
									}
								]

							}
						]

					},
					{
						:name => "Regular Expressions",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "re-pattern",
										:ns => "clojure.core",
										:link => "/v/1496",
										:id => 1496
									},
									{
										:name => "re-matcher",
										:ns => "clojure.core",
										:link => "/v/1628",
										:id => 1628
									}
								]

							},
							{
								:name => "Use",
								:symbols => [
									{
										:name => "re-find",
										:ns => "clojure.core",
										:link => "/v/1773",
										:id => 1773
									},
									{
										:name => "re-matches",
										:ns => "clojure.core",
										:link => "/v/1613",
										:id => 1613
									},
									{
										:name => "re-seq",
										:ns => "clojure.core",
										:link => "/v/1792",
										:id => 1792
									},
									{
										:name => "re-groups",
										:ns => "clojure.core",
										:link => "/v/1679",
										:id => 1679
									}
								]

							}
						]

					}
				]

			},
			{
				:name => "Operations",
				:categories => [
					{
						:name => "Flow Control",
						:groups => [
							{
								:name => "Normal",
								:symbols => [
									{
										:name => "if",
										:ns => "clojure.core",
										:link => "/v/5269",
										:id => 5269
									},
									{
										:name => "if-not",
										:ns => "clojure.core",
										:link => "/v/1537",
										:id => 1537
									},
									{
										:name => "if-let",
										:ns => "clojure.core",
										:link => "/v/1953",
										:id => 1953
									},
									{
										:name => "when",
										:ns => "clojure.core",
										:link => "/v/2050",
										:id => 2050
									},
									{
										:name => "when-not",
										:ns => "clojure.core",
										:link => "/v/1657",
										:id => 1657
									},
									{
										:name => "when-let",
										:ns => "clojure.core",
										:link => "/v/1849",
										:id => 1849
									},
									{
										:name => "when-first",
										:ns => "clojure.core",
										:link => "/v/1751",
										:id => 1751
									},
									{
										:name => "cond",
										:ns => "clojure.core",
										:link => "/v/1553",
										:id => 1553
									},
									{
										:name => "condp",
										:ns => "clojure.core",
										:link => "/v/1838",
										:id => 1838
									},
									{
										:name => "case",
										:ns => "clojure.core",
										:link => "/v/1893",
										:id => 1893
									},
									{
										:name => "do",
										:ns => "clojure.core",
										:link => "/v/5270",
										:id => 5270
									},
									{
										:name => "eval",
										:ns => "clojure.core",
										:link => "/v/2022",
										:id => 2022
									},
									{
										:name => "loop",
										:ns => "clojure.core",
										:link => "/v/1617",
										:id => 1617
									},
									{
										:name => "recur",
										:ns => "clojure.core",
										:link => "/v/5273",
										:id => 5273
									},
									{
										:name => "trampoline",
										:ns => "clojure.core",
										:link => "/v/2070",
										:id => 2070
									},
									{
										:name => "while",
										:ns => "clojure.core",
										:link => "/v/1881",
										:id => 1881
									}
								]

							},
							{
								:name => "Exceptional",
								:symbols => [
									{
										:name => "try",
										:ns => "clojure.core",
										:link => "/v/5275",
										:id => 5275
									},
									{
										:name => "catch",
										:ns => "clojure.core",
										:link => "/v/5281",
										:id => 5281
									},
									{
										:name => "finally",
										:ns => "clojure.core",
										:link => "/v/5282",
										:id => 5282
									},
									{
										:name => "throw",
										:ns => "clojure.core",
										:link => "/v/5274",
										:id => 5274
									},
									{
										:name => "assert",
										:ns => "clojure.core",
										:link => "/v/1733",
										:id => 1733
									}
								]

							},
							{
								:name => "Delay",
								:symbols => [
									{
										:name => "delay",
										:ns => "clojure.core",
										:link => "/v/1986",
										:id => 1986
									},
									{
										:name => "delay?",
										:ns => "clojure.core",
										:link => "/v/2033",
										:id => 2033
									},
									{
										:name => "deref",
										:ns => "clojure.core",
										:link => "/v/1630",
										:id => 1630
									},
									{
										:name => "force",
										:ns => "clojure.core",
										:link => "/v/1950",
										:id => 1950
									}
								]

							},
							{
								:name => "Function Based",
								:symbols => [
									{
										:name => "repeatedly",
										:ns => "clojure.core",
										:link => "/v/2069",
										:id => 2069
									},
									{
										:name => "iterate",
										:ns => "clojure.core",
										:link => "/v/1749",
										:id => 1749
									}
								]

							},
							{
								:name => "Sequence Based",
								:symbols => [
									{
										:name => "dotimes",
										:ns => "clojure.core",
										:link => "/v/1587",
										:id => 1587
									},
									{
										:name => "doseq",
										:ns => "clojure.core",
										:link => "/v/1534",
										:id => 1534
									},
									{
										:name => "for",
										:ns => "clojure.core",
										:link => "/v/1548",
										:id => 1548
									}
								]

							},
							{
								:name => "Laziness",
								:symbols => [
									{
										:name => "lazy-seq",
										:ns => "clojure.core",
										:link => "/v/2044",
										:id => 2044
									},
									{
										:name => "lazy-cat",
										:ns => "clojure.core",
										:link => "/v/1599",
										:id => 1599
									},
									{
										:name => "doall",
										:ns => "clojure.core",
										:link => "/v/1806",
										:id => 1806
									},
									{
										:name => "dorun",
										:ns => "clojure.core",
										:link => "/v/1526",
										:id => 1526
									}
								]

							}
						]

					},
					{
						:name => "Type Inspection",
						:groups => [
							{
								:name => "Clojure Types",
								:symbols => [
									{
										:name => "type",
										:ns => "clojure.core",
										:link => "/v/2068",
										:id => 2068
									},
									{
										:name => "extends?",
										:ns => "clojure.core",
										:link => "/v/1714",
										:id => 1714
									},
									{
										:name => "satisfies?",
										:ns => "clojure.core",
										:link => "/v/2086",
										:id => 2086
									}
								]

							},
							{
								:name => "Java Types",
								:symbols => [
									{
										:name => "class",
										:ns => "clojure.core",
										:link => "/v/1791",
										:id => 1791
									},
									{
										:name => "bases",
										:ns => "clojure.core",
										:link => "/v/1697",
										:id => 1697
									},
									{
										:name => "supers",
										:ns => "clojure.core",
										:link => "/v/1937",
										:id => 1937
									},
									{
										:name => "class?",
										:ns => "clojure.core",
										:link => "/v/1744",
										:id => 1744
									},
									{
										:name => "instance?",
										:ns => "clojure.core",
										:link => "/v/1519",
										:id => 1519
									},
									{
										:name => "isa?",
										:ns => "clojure.core",
										:link => "/v/1711",
										:id => 1711
									},
									{
										:name => "cast",
										:ns => "clojure.core",
										:link => "/v/1935",
										:id => 1935
									}
								]

							}
						]

					},
					{
						:name => "Concurrency",
						:groups => [
							{
								:name => "General",
								:symbols => [
									{
										:name => "deref",
										:ns => "clojure.core",
										:link => "/v/1630",
										:id => 1630
									},
									{
										:name => "get-validator",
										:ns => "clojure.core",
										:link => "/v/1586",
										:id => 1586
									},
									{
										:name => "set-validator!",
										:ns => "clojure.core",
										:link => "/v/1891",
										:id => 1891
									}
								]

							},
							{
								:name => "Atoms",
								:symbols => [
									{
										:name => "atom",
										:ns => "clojure.core",
										:link => "/v/2028",
										:id => 2028
									},
									{
										:name => "swap!",
										:ns => "clojure.core",
										:link => "/v/1988",
										:id => 1988
									},
									{
										:name => "reset!",
										:ns => "clojure.core",
										:link => "/v/1925",
										:id => 1925
									},
									{
										:name => "compare-and-set!",
										:ns => "clojure.core",
										:link => "/v/2066",
										:id => 2066
									}
								]

							},
							{
								:name => "Refs",
								:symbols => [
									{
										:name => "ref",
										:ns => "clojure.core",
										:link => "/v/1760",
										:id => 1760
									},
									{
										:name => "sync",
										:ns => "clojure.core",
										:link => "/v/1574",
										:id => 1574
									},
									{
										:name => "dosync",
										:ns => "clojure.core",
										:link => "/v/1564",
										:id => 1564
									},
									{
										:name => "ref-set",
										:ns => "clojure.core",
										:link => "/v/1701",
										:id => 1701
									},
									{
										:name => "alter",
										:ns => "clojure.core",
										:link => "/v/1649",
										:id => 1649
									},
									{
										:name => "commute",
										:ns => "clojure.core",
										:link => "/v/1600",
										:id => 1600
									},
									{
										:name => "ensure",
										:ns => "clojure.core",
										:link => "/v/1898",
										:id => 1898
									},
									{
										:name => "io!",
										:ns => "clojure.core",
										:link => "/v/2036",
										:id => 2036
									},
									{
										:name => "ref-history-count",
										:ns => "clojure.core",
										:link => "/v/1718",
										:id => 1718
									},
									{
										:name => "ref-max-history",
										:ns => "clojure.core",
										:link => "/v/1588",
										:id => 1588
									},
									{
										:name => "ref-min-history",
										:ns => "clojure.core",
										:link => "/v/1776",
										:id => 1776
									}
								]

							},
							{
								:name => "Agents",
								:symbols => [
									{
										:name => "agent",
										:ns => "clojure.core",
										:link => "/v/2098",
										:id => 2098
									},
									{
										:name => "send",
										:ns => "clojure.core",
										:link => "/v/1842",
										:id => 1842
									},
									{
										:name => "send-off",
										:ns => "clojure.core",
										:link => "/v/1663",
										:id => 1663
									},
									{
										:name => "await",
										:ns => "clojure.core",
										:link => "/v/1943",
										:id => 1943
									},
									{
										:name => "await-for",
										:ns => "clojure.core",
										:link => "/v/2107",
										:id => 2107
									},
									{
										:name => "agent-error",
										:ns => "clojure.core",
										:link => "/v/1671",
										:id => 1671
									},
									{
										:name => "restart-agent",
										:ns => "clojure.core",
										:link => "/v/1754",
										:id => 1754
									},
									{
										:name => "shutdown-agents",
										:ns => "clojure.core",
										:link => "/v/1924",
										:id => 1924
									},
									{
										:name => "*agent*",
										:ns => "clojure.core",
										:link => "/v/2008",
										:id => 2008
									},
									{
										:name => "error-handler",
										:ns => "clojure.core",
										:link => "/v/2096",
										:id => 2096
									},
									{
										:name => "set-error-handler!",
										:ns => "clojure.core",
										:link => "/v/2071",
										:id => 2071
									},
									{
										:name => "error-mode",
										:ns => "clojure.core",
										:link => "/v/1552",
										:id => 1552
									},
									{
										:name => "set-error-mode!",
										:ns => "clojure.core",
										:link => "/v/1998",
										:id => 1998
									},
									{
										:name => "release-pending-sends",
										:ns => "clojure.core",
										:link => "/v/2002",
										:id => 2002
									}
								]

							},
							{
								:name => "Futures",
								:symbols => [
									{
										:name => "future",
										:ns => "clojure.core",
										:link => "/v/1677",
										:id => 1677
									},
									{
										:name => "future-call",
										:ns => "clojure.core",
										:link => "/v/1712",
										:id => 1712
									},
									{
										:name => "future-done?",
										:ns => "clojure.core",
										:link => "/v/1618",
										:id => 1618
									},
									{
										:name => "future-cancel",
										:ns => "clojure.core",
										:link => "/v/1764",
										:id => 1764
									},
									{
										:name => "future-cancelled?",
										:ns => "clojure.core",
										:link => "/v/1931",
										:id => 1931
									},
									{
										:name => "future?",
										:ns => "clojure.core",
										:link => "/v/1722",
										:id => 1722
									}
								]

							},
							{
								:name => "Thread Local Values",
								:symbols => [
									{
										:name => "bound-fn",
										:ns => "clojure.core",
										:link => "/v/1525",
										:id => 1525
									},
									{
										:name => "bound-fn*",
										:ns => "clojure.core",
										:link => "/v/1767",
										:id => 1767
									},
									{
										:name => "get-thread-bindings",
										:ns => "clojure.core",
										:link => "/v/1758",
										:id => 1758
									},
									{
										:name => "push-thread-bindings",
										:ns => "clojure.core",
										:link => "/v/1912",
										:id => 1912
									},
									{
										:name => "pop-thread-bindings",
										:ns => "clojure.core",
										:link => "/v/1551",
										:id => 1551
									},
									{
										:name => "thread-bound?",
										:ns => "clojure.core",
										:link => "/v/1609",
										:id => 1609
									}
								]

							},
							{
								:name => "Misc",
								:symbols => [
									{
										:name => "locking",
										:ns => "clojure.core",
										:link => "/v/1971",
										:id => 1971
									},
									{
										:name => "pcalls",
										:ns => "clojure.core",
										:link => "/v/1938",
										:id => 1938
									},
									{
										:name => "pvalues",
										:ns => "clojure.core",
										:link => "/v/1780",
										:id => 1780
									},
									{
										:name => "pmap",
										:ns => "clojure.core",
										:link => "/v/1952",
										:id => 1952
									},
									{
										:name => "seque",
										:ns => "clojure.core",
										:link => "/v/1703",
										:id => 1703
									},
									{
										:name => "promise",
										:ns => "clojure.core",
										:link => "/v/1591",
										:id => 1591
									},
									{
										:name => "deliver",
										:ns => "clojure.core",
										:link => "/v/1623",
										:id => 1623
									},
									{
										:name => "add-watch",
										:ns => "clojure.core",
										:link => "/v/1539",
										:id => 1539
									},
									{
										:name => "remove-watch",
										:ns => "clojure.core",
										:link => "/v/1715",
										:id => 1715
									}
								]

							}
						]

					}
				]

			},
			{
				:name => "Functions",
				:categories => [
					{
						:name => "General",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "fn",
										:ns => "clojure.core",
										:link => "/v/1557",
										:id => 1557
									},
									{
										:name => "defn",
										:ns => "clojure.core",
										:link => "/v/1833",
										:id => 1833
									},
									{
										:name => "defn-",
										:ns => "clojure.core",
										:link => "/v/1771",
										:id => 1771
									},
									{
										:name => "definline",
										:ns => "clojure.core",
										:link => "/v/1766",
										:id => 1766
									},
									{
										:name => "identity",
										:ns => "clojure.core",
										:link => "/v/1905",
										:id => 1905
									},
									{
										:name => "constantly",
										:ns => "clojure.core",
										:link => "/v/1856",
										:id => 1856
									},
									{
										:name => "memfn",
										:ns => "clojure.core",
										:link => "/v/1737",
										:id => 1737
									},
									{
										:name => "comp",
										:ns => "clojure.core",
										:link => "/v/1850",
										:id => 1850
									},
									{
										:name => "complement",
										:ns => "clojure.core",
										:link => "/v/1584",
										:id => 1584
									},
									{
										:name => "partial",
										:ns => "clojure.core",
										:link => "/v/1951",
										:id => 1951
									},
									{
										:name => "juxt",
										:ns => "clojure.core",
										:link => "/v/2058",
										:id => 2058
									},
									{
										:name => "memoize",
										:ns => "clojure.core",
										:link => "/v/1886",
										:id => 1886
									}
								]

							},
							{
								:name => "Call",
								:symbols => [
									{
										:name => "->",
										:ns => "clojure.core",
										:link => "/v/1872",
										:id => 1872
									},
									{
										:name => "->>",
										:ns => "clojure.core",
										:link => "/v/1660",
										:id => 1660
									},
									{
										:name => "apply",
										:ns => "clojure.core",
										:link => "/v/1987",
										:id => 1987
									}
								]

							},
							{
								:name => "Test",
								:symbols => [
									{
										:name => "fn?",
										:ns => "clojure.core",
										:link => "/v/1523",
										:id => 1523
									},
									{
										:name => "ifn?",
										:ns => "clojure.core",
										:link => "/v/1982",
										:id => 1982
									}
								]

							}
						]

					},
					{
						:name => "Multifunctions",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "defmulti",
										:ns => "clojure.core",
										:link => "/v/1989",
										:id => 1989
									},
									{
										:name => "defmethod",
										:ns => "clojure.core",
										:link => "/v/1592",
										:id => 1592
									}
								]

							},
							{
								:name => "Inspect and Modify",
								:symbols => [
									{
										:name => "get-method",
										:ns => "clojure.core",
										:link => "/v/2097",
										:id => 2097
									},
									{
										:name => "methods",
										:ns => "clojure.core",
										:link => "/v/1930",
										:id => 1930
									},
									{
										:name => "prefer-method",
										:ns => "clojure.core",
										:link => "/v/1896",
										:id => 1896
									},
									{
										:name => "prefers",
										:ns => "clojure.core",
										:link => "/v/1836",
										:id => 1836
									},
									{
										:name => "remove-method",
										:ns => "clojure.core",
										:link => "/v/1528",
										:id => 1528
									},
									{
										:name => "remove-all-methods",
										:ns => "clojure.core",
										:link => "/v/1882",
										:id => 1882
									}
								]

							}
						]

					},
					{
						:name => "Macros",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "defmacro",
										:ns => "clojure.core",
										:link => "/v/1890",
										:id => 1890
									},
									{
										:name => "macroexpand",
										:ns => "clojure.core",
										:link => "/v/1874",
										:id => 1874
									},
									{
										:name => "macroexpand-1",
										:ns => "clojure.core",
										:link => "/v/1808",
										:id => 1808
									},
									{
										:name => "gensym",
										:ns => "clojure.core",
										:link => "/v/1529",
										:id => 1529
									}
								]

							}
						]

					},
					{
						:name => "Java Interop",
						:groups => [
							{
								:name => "Use",
								:symbols => [
									{
										:name => "doto",
										:ns => "clojure.core",
										:link => "/v/1713",
										:id => 1713
									},
									{
										:name => "..",
										:ns => "clojure.core",
										:link => "/v/1667",
										:id => 1667
									},
									{
										:name => "set!",
										:ns => "clojure.core",
										:link => "/v/5280",
										:id => 5280
									}
								]

							},
							{
								:name => "Arrays",
								:symbols => [
									{
										:name => "make-array",
										:ns => "clojure.core",
										:link => "/v/2054",
										:id => 2054
									},
									{
										:name => "object-array",
										:ns => "clojure.core",
										:link => "/v/1504",
										:id => 1504
									},
									{
										:name => "boolean-array",
										:ns => "clojure.core",
										:link => "/v/1544",
										:id => 1544
									},
									{
										:name => "byte-array",
										:ns => "clojure.core",
										:link => "/v/1993",
										:id => 1993
									},
									{
										:name => "char-array",
										:ns => "clojure.core",
										:link => "/v/1563",
										:id => 1563
									},
									{
										:name => "short-array",
										:ns => "clojure.core",
										:link => "/v/1559",
										:id => 1559
									},
									{
										:name => "int-array",
										:ns => "clojure.core",
										:link => "/v/1730",
										:id => 1730
									},
									{
										:name => "long-array",
										:ns => "clojure.core",
										:link => "/v/1550",
										:id => 1550
									},
									{
										:name => "float-array",
										:ns => "clojure.core",
										:link => "/v/1709",
										:id => 1709
									},
									{
										:name => "double-array",
										:ns => "clojure.core",
										:link => "/v/1738",
										:id => 1738
									},
									{
										:name => "aclone",
										:ns => "clojure.core",
										:link => "/v/2024",
										:id => 2024
									},
									{
										:name => "to-array",
										:ns => "clojure.core",
										:link => "/v/2063",
										:id => 2063
									},
									{
										:name => "to-array-2d",
										:ns => "clojure.core",
										:link => "/v/1782",
										:id => 1782
									},
									{
										:name => "into-array",
										:ns => "clojure.core",
										:link => "/v/1643",
										:id => 1643
									}
								]

							},
							{
								:name => "Use",
								:symbols => [
									{
										:name => "aget",
										:ns => "clojure.core",
										:link => "/v/2016",
										:id => 2016
									},
									{
										:name => "aset",
										:ns => "clojure.core",
										:link => "/v/1804",
										:id => 1804
									},
									{
										:name => "aset-boolean",
										:ns => "clojure.core",
										:link => "/v/1879",
										:id => 1879
									},
									{
										:name => "aset-char",
										:ns => "clojure.core",
										:link => "/v/1705",
										:id => 1705
									},
									{
										:name => "aset-byte",
										:ns => "clojure.core",
										:link => "/v/1536",
										:id => 1536
									},
									{
										:name => "aset-int",
										:ns => "clojure.core",
										:link => "/v/1980",
										:id => 1980
									},
									{
										:name => "aset-long",
										:ns => "clojure.core",
										:link => "/v/2009",
										:id => 2009
									},
									{
										:name => "aset-short",
										:ns => "clojure.core",
										:link => "/v/1747",
										:id => 1747
									},
									{
										:name => "aset-float",
										:ns => "clojure.core",
										:link => "/v/1596",
										:id => 1596
									},
									{
										:name => "aset-double",
										:ns => "clojure.core",
										:link => "/v/1892",
										:id => 1892
									},
									{
										:name => "alength",
										:ns => "clojure.core",
										:link => "/v/2059",
										:id => 2059
									},
									{
										:name => "amap",
										:ns => "clojure.core",
										:link => "/v/1871",
										:id => 1871
									},
									{
										:name => "areduce",
										:ns => "clojure.core",
										:link => "/v/2052",
										:id => 2052
									}
								]

							},
							{
								:name => "Cast",
								:symbols => [
									{
										:name => "booleans",
										:ns => "clojure.core",
										:link => "/v/1509",
										:id => 1509
									},
									{
										:name => "bytes",
										:ns => "clojure.core",
										:link => "/v/1945",
										:id => 1945
									},
									{
										:name => "chars",
										:ns => "clojure.core",
										:link => "/v/1743",
										:id => 1743
									},
									{
										:name => "ints",
										:ns => "clojure.core",
										:link => "/v/1907",
										:id => 1907
									},
									{
										:name => "shorts",
										:ns => "clojure.core",
										:link => "/v/1517",
										:id => 1517
									},
									{
										:name => "longs",
										:ns => "clojure.core",
										:link => "/v/1514",
										:id => 1514
									},
									{
										:name => "floats",
										:ns => "clojure.core",
										:link => "/v/1653",
										:id => 1653
									},
									{
										:name => "doubles",
										:ns => "clojure.core",
										:link => "/v/1710",
										:id => 1710
									}
								]

							}
						]

					},
					{
						:name => "Proxies",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "proxy",
										:ns => "clojure.core",
										:link => "/v/1990",
										:id => 1990
									},
									{
										:name => "get-proxy-class",
										:ns => "clojure.core",
										:link => "/v/1847",
										:id => 1847
									},
									{
										:name => "construct-proxy",
										:ns => "clojure.core",
										:link => "/v/1775",
										:id => 1775
									},
									{
										:name => "init-proxy",
										:ns => "clojure.core",
										:link => "/v/1568",
										:id => 1568
									}
								]

							},
							{
								:name => "Misc",
								:symbols => [
									{
										:name => "proxy-mappings",
										:ns => "clojure.core",
										:link => "/v/1904",
										:id => 1904
									},
									{
										:name => "proxy-super",
										:ns => "clojure.core",
										:link => "/v/1729",
										:id => 1729
									},
									{
										:name => "update-proxy",
										:ns => "clojure.core",
										:link => "/v/1976",
										:id => 1976
									}
								]

							}
						]

					}
				]

			},
			{
				:name => "Collections / Sequences",
				:categories => [
					{
						:name => "Collections",
						:groups => [
							{
								:name => "Generic Operations",
								:symbols => [
									{
										:name => "count",
										:ns => "clojure.core",
										:link => "/v/1846",
										:id => 1846
									},
									{
										:name => "empty",
										:ns => "clojure.core",
										:link => "/v/1524",
										:id => 1524
									},
									{
										:name => "not-empty",
										:ns => "clojure.core",
										:link => "/v/2077",
										:id => 2077
									},
									{
										:name => "into",
										:ns => "clojure.core",
										:link => "/v/1813",
										:id => 1813
									},
									{
										:name => "conj",
										:ns => "clojure.core",
										:link => "/v/1696",
										:id => 1696
									}
								]

							},
							{
								:name => "Content Tests",
								:symbols => [
									{
										:name => "contains?",
										:ns => "clojure.core",
										:link => "/v/1975",
										:id => 1975
									},
									{
										:name => "distinct?",
										:ns => "clojure.core",
										:link => "/v/1686",
										:id => 1686
									},
									{
										:name => "empty?",
										:ns => "clojure.core",
										:link => "/v/1795",
										:id => 1795
									},
									{
										:name => "every?",
										:ns => "clojure.core",
										:link => "/v/2085",
										:id => 2085
									},
									{
										:name => "not-every?",
										:ns => "clojure.core",
										:link => "/v/1900",
										:id => 1900
									},
									{
										:name => "some",
										:ns => "clojure.core",
										:link => "/v/1541",
										:id => 1541
									},
									{
										:name => "not-any?",
										:ns => "clojure.core",
										:link => "/v/1809",
										:id => 1809
									}
								]

							},
							{
								:name => "Capabilities",
								:symbols => [
									{
										:name => "sequential?",
										:ns => "clojure.core",
										:link => "/v/1522",
										:id => 1522
									},
									{
										:name => "associative?",
										:ns => "clojure.core",
										:link => "/v/1828",
										:id => 1828
									},
									{
										:name => "sorted?",
										:ns => "clojure.core",
										:link => "/v/1558",
										:id => 1558
									},
									{
										:name => "counted?",
										:ns => "clojure.core",
										:link => "/v/1736",
										:id => 1736
									},
									{
										:name => "reversible?",
										:ns => "clojure.core",
										:link => "/v/1661",
										:id => 1661
									}
								]

							},
							{
								:name => "Type Tests",
								:symbols => [
									{
										:name => "coll?",
										:ns => "clojure.core",
										:link => "/v/2074",
										:id => 2074
									},
									{
										:name => "seq?",
										:ns => "clojure.core",
										:link => "/v/1664",
										:id => 1664
									},
									{
										:name => "vector?",
										:ns => "clojure.core",
										:link => "/v/1723",
										:id => 1723
									},
									{
										:name => "list?",
										:ns => "clojure.core",
										:link => "/v/1801",
										:id => 1801
									},
									{
										:name => "map?",
										:ns => "clojure.core",
										:link => "/v/1835",
										:id => 1835
									},
									{
										:name => "set?",
										:ns => "clojure.core",
										:link => "/v/2053",
										:id => 2053
									}
								]

							}
						]

					},
					{
						:name => "Vectors",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "vec",
										:ns => "clojure.core",
										:link => "/v/1690",
										:id => 1690
									},
									{
										:name => "vector",
										:ns => "clojure.core",
										:link => "/v/1693",
										:id => 1693
									},
									{
										:name => "vector-of",
										:ns => "clojure.core",
										:link => "/v/1503",
										:id => 1503
									}
								]

							},
							{
								:name => "Use",
								:symbols => [
									{
										:name => "conj",
										:ns => "clojure.core",
										:link => "/v/1696",
										:id => 1696
									},
									{
										:name => "peek",
										:ns => "clojure.core",
										:link => "/v/1864",
										:id => 1864
									},
									{
										:name => "pop",
										:ns => "clojure.core",
										:link => "/v/2026",
										:id => 2026
									},
									{
										:name => "get",
										:ns => "clojure.core",
										:link => "/v/1941",
										:id => 1941
									},
									{
										:name => "assoc",
										:ns => "clojure.core",
										:link => "/v/1702",
										:id => 1702
									},
									{
										:name => "subvec",
										:ns => "clojure.core",
										:link => "/v/1992",
										:id => 1992
									},
									{
										:name => "rseq",
										:ns => "clojure.core",
										:link => "/v/1662",
										:id => 1662
									}
								]

							}
						]

					},
					{
						:name => "Lists",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "list",
										:ns => "clojure.core",
										:link => "/v/2082",
										:id => 2082
									},
									{
										:name => "list*",
										:ns => "clojure.core",
										:link => "/v/1507",
										:id => 1507
									}
								]

							},
							{
								:name => "Use",
								:symbols => [
									{
										:name => "cons",
										:ns => "clojure.core",
										:link => "/v/1873",
										:id => 1873
									},
									{
										:name => "conj",
										:ns => "clojure.core",
										:link => "/v/1696",
										:id => 1696
									},
									{
										:name => "peek",
										:ns => "clojure.core",
										:link => "/v/1864",
										:id => 1864
									},
									{
										:name => "pop",
										:ns => "clojure.core",
										:link => "/v/2026",
										:id => 2026
									},
									{
										:name => "first",
										:ns => "clojure.core",
										:link => "/v/1883",
										:id => 1883
									},
									{
										:name => "rest",
										:ns => "clojure.core",
										:link => "/v/1994",
										:id => 1994
									}
								]

							}
						]

					},
					{
						:name => "Maps",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "hash-map",
										:ns => "clojure.core",
										:link => "/v/2064",
										:id => 2064
									},
									{
										:name => "array-map",
										:ns => "clojure.core",
										:link => "/v/2012",
										:id => 2012
									},
									{
										:name => "zipmap",
										:ns => "clojure.core",
										:link => "/v/1579",
										:id => 1579
									},
									{
										:name => "sorted-map",
										:ns => "clojure.core",
										:link => "/v/1494",
										:id => 1494
									},
									{
										:name => "sorted-map-by",
										:ns => "clojure.core",
										:link => "/v/1783",
										:id => 1783
									},
									{
										:name => "bean",
										:ns => "clojure.core",
										:link => "/v/1884",
										:id => 1884
									},
									{
										:name => "frequencies",
										:ns => "clojure.core",
										:link => "/v/1742",
										:id => 1742
									}
								]

							},
							{
								:name => "Use",
								:symbols => [
									{
										:name => "assoc",
										:ns => "clojure.core",
										:link => "/v/1702",
										:id => 1702
									},
									{
										:name => "assoc-in",
										:ns => "clojure.core",
										:link => "/v/1757",
										:id => 1757
									},
									{
										:name => "dissoc",
										:ns => "clojure.core",
										:link => "/v/2091",
										:id => 2091
									},
									{
										:name => "find",
										:ns => "clojure.core",
										:link => "/v/2073",
										:id => 2073
									},
									{
										:name => "key",
										:ns => "clojure.core",
										:link => "/v/1790",
										:id => 1790
									},
									{
										:name => "val",
										:ns => "clojure.core",
										:link => "/v/1500",
										:id => 1500
									},
									{
										:name => "keys",
										:ns => "clojure.core",
										:link => "/v/1547",
										:id => 1547
									},
									{
										:name => "vals",
										:ns => "clojure.core",
										:link => "/v/1625",
										:id => 1625
									},
									{
										:name => "get",
										:ns => "clojure.core",
										:link => "/v/1941",
										:id => 1941
									},
									{
										:name => "get-in",
										:ns => "clojure.core",
										:link => "/v/1581",
										:id => 1581
									},
									{
										:name => "update-in",
										:ns => "clojure.core",
										:link => "/v/1692",
										:id => 1692
									},
									{
										:name => "select-keys",
										:ns => "clojure.core",
										:link => "/v/1627",
										:id => 1627
									},
									{
										:name => "merge",
										:ns => "clojure.core",
										:link => "/v/2101",
										:id => 2101
									},
									{
										:name => "merge-with",
										:ns => "clojure.core",
										:link => "/v/2039",
										:id => 2039
									}
								]

							},
							{
								:name => "Use (Sorted Maps)",
								:symbols => [
									{
										:name => "rseq",
										:ns => "clojure.core",
										:link => "/v/1662",
										:id => 1662
									},
									{
										:name => "subseq",
										:ns => "clojure.core",
										:link => "/v/2102",
										:id => 2102
									},
									{
										:name => "subseq",
										:ns => "clojure.core",
										:link => "/v/2102",
										:id => 2102
									},
									{
										:name => "rsubseq",
										:ns => "clojure.core",
										:link => "/v/1719",
										:id => 1719
									},
									{
										:name => "rsubseq",
										:ns => "clojure.core",
										:link => "/v/1719",
										:id => 1719
									}
								]

							}
						]

					},
					{
						:name => "Sets",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "hash-set",
										:ns => "clojure.core",
										:link => "/v/1538",
										:id => 1538
									},
									{
										:name => "set",
										:ns => "clojure.core",
										:link => "/v/1848",
										:id => 1848
									},
									{
										:name => "sorted-set",
										:ns => "clojure.core",
										:link => "/v/2099",
										:id => 2099
									},
									{
										:name => "sorted-set-by",
										:ns => "clojure.core",
										:link => "/v/1934",
										:id => 1934
									}
								]

							},
							{
								:name => "Use",
								:symbols => [
									{
										:name => "conj",
										:ns => "clojure.core",
										:link => "/v/1696",
										:id => 1696
									},
									{
										:name => "disj",
										:ns => "clojure.core",
										:link => "/v/2035",
										:id => 2035
									},
									{
										:name => "get",
										:ns => "clojure.core",
										:link => "/v/1941",
										:id => 1941
									}
								]

							}
						]

					},
					{
						:name => "Structs",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "defstruct",
										:ns => "clojure.core",
										:link => "/v/1601",
										:id => 1601
									},
									{
										:name => "create-struct",
										:ns => "clojure.core",
										:link => "/v/1727",
										:id => 1727
									},
									{
										:name => "struct",
										:ns => "clojure.core",
										:link => "/v/2011",
										:id => 2011
									},
									{
										:name => "struct-map",
										:ns => "clojure.core",
										:link => "/v/1901",
										:id => 1901
									},
									{
										:name => "accessor",
										:ns => "clojure.core",
										:link => "/v/1740",
										:id => 1740
									}
								]

							},
							{
								:name => "Use",
								:symbols => [
									{
										:name => "get",
										:ns => "clojure.core",
										:link => "/v/1941",
										:id => 1941
									},
									{
										:name => "assoc",
										:ns => "clojure.core",
										:link => "/v/1702",
										:id => 1702
									}
								]

							}
						]

					},
					{
						:name => "Sequences",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "seq",
										:ns => "clojure.core",
										:link => "/v/1778",
										:id => 1778
									},
									{
										:name => "sequence",
										:ns => "clojure.core",
										:link => "/v/1636",
										:id => 1636
									},
									{
										:name => "repeat",
										:ns => "clojure.core",
										:link => "/v/1578",
										:id => 1578
									},
									{
										:name => "replicate",
										:ns => "clojure.core",
										:link => "/v/1682",
										:id => 1682
									},
									{
										:name => "range",
										:ns => "clojure.core",
										:link => "/v/1888",
										:id => 1888
									},
									{
										:name => "repeatedly",
										:ns => "clojure.core",
										:link => "/v/2069",
										:id => 2069
									},
									{
										:name => "iterate",
										:ns => "clojure.core",
										:link => "/v/1749",
										:id => 1749
									},
									{
										:name => "lazy-seq",
										:ns => "clojure.core",
										:link => "/v/2044",
										:id => 2044
									},
									{
										:name => "lazy-cat",
										:ns => "clojure.core",
										:link => "/v/1599",
										:id => 1599
									},
									{
										:name => "cycle",
										:ns => "clojure.core",
										:link => "/v/1863",
										:id => 1863
									},
									{
										:name => "interleave",
										:ns => "clojure.core",
										:link => "/v/1869",
										:id => 1869
									},
									{
										:name => "interpose",
										:ns => "clojure.core",
										:link => "/v/1978",
										:id => 1978
									},
									{
										:name => "tree-seq",
										:ns => "clojure.core",
										:link => "/v/1889",
										:id => 1889
									},
									{
										:name => "xml-seq",
										:ns => "clojure.core",
										:link => "/v/1689",
										:id => 1689
									},
									{
										:name => "enumeration-seq",
										:ns => "clojure.core",
										:link => "/v/1895",
										:id => 1895
									},
									{
										:name => "iterator-seq",
										:ns => "clojure.core",
										:link => "/v/1966",
										:id => 1966
									},
									{
										:name => "file-seq",
										:ns => "clojure.core",
										:link => "/v/1841",
										:id => 1841
									},
									{
										:name => "line-seq",
										:ns => "clojure.core",
										:link => "/v/2048",
										:id => 2048
									},
									{
										:name => "resultset-seq",
										:ns => "clojure.core",
										:link => "/v/1810",
										:id => 1810
									}
								]

							},
							{
								:name => "Use (General)",
								:symbols => [
									{
										:name => "first",
										:ns => "clojure.core",
										:link => "/v/1883",
										:id => 1883
									},
									{
										:name => "second",
										:ns => "clojure.core",
										:link => "/v/1545",
										:id => 1545
									},
									{
										:name => "last",
										:ns => "clojure.core",
										:link => "/v/1644",
										:id => 1644
									},
									{
										:name => "rest",
										:ns => "clojure.core",
										:link => "/v/1994",
										:id => 1994
									},
									{
										:name => "next",
										:ns => "clojure.core",
										:link => "/v/1620",
										:id => 1620
									},
									{
										:name => "ffirst",
										:ns => "clojure.core",
										:link => "/v/1818",
										:id => 1818
									},
									{
										:name => "nfirst",
										:ns => "clojure.core",
										:link => "/v/2005",
										:id => 2005
									},
									{
										:name => "fnext",
										:ns => "clojure.core",
										:link => "/v/1908",
										:id => 1908
									},
									{
										:name => "nnext",
										:ns => "clojure.core",
										:link => "/v/1805",
										:id => 1805
									},
									{
										:name => "nth",
										:ns => "clojure.core",
										:link => "/v/1851",
										:id => 1851
									},
									{
										:name => "nthnext",
										:ns => "clojure.core",
										:link => "/v/2006",
										:id => 2006
									},
									{
										:name => "rand-nth",
										:ns => "clojure.core",
										:link => "/v/2055",
										:id => 2055
									},
									{
										:name => "butlast",
										:ns => "clojure.core",
										:link => "/v/1658",
										:id => 1658
									},
									{
										:name => "take",
										:ns => "clojure.core",
										:link => "/v/2049",
										:id => 2049
									},
									{
										:name => "take-last",
										:ns => "clojure.core",
										:link => "/v/2047",
										:id => 2047
									},
									{
										:name => "take-nth",
										:ns => "clojure.core",
										:link => "/v/2040",
										:id => 2040
									},
									{
										:name => "take-while",
										:ns => "clojure.core",
										:link => "/v/1958",
										:id => 1958
									},
									{
										:name => "drop",
										:ns => "clojure.core",
										:link => "/v/2018",
										:id => 2018
									},
									{
										:name => "drop-last",
										:ns => "clojure.core",
										:link => "/v/1830",
										:id => 1830
									},
									{
										:name => "drop-while",
										:ns => "clojure.core",
										:link => "/v/2075",
										:id => 2075
									},
									{
										:name => "keep",
										:ns => "clojure.core",
										:link => "/v/2108",
										:id => 2108
									},
									{
										:name => "keep-indexed",
										:ns => "clojure.core",
										:link => "/v/1683",
										:id => 1683
									}
								]

							},
							{
								:name => "Use ('Modification')",
								:symbols => [
									{
										:name => "conj",
										:ns => "clojure.core",
										:link => "/v/1696",
										:id => 1696
									},
									{
										:name => "concat",
										:ns => "clojure.core",
										:link => "/v/1691",
										:id => 1691
									},
									{
										:name => "distinct",
										:ns => "clojure.core",
										:link => "/v/1580",
										:id => 1580
									},
									{
										:name => "group-by",
										:ns => "clojure.core",
										:link => "/v/1940",
										:id => 1940
									},
									{
										:name => "partition",
										:ns => "clojure.core",
										:link => "/v/1972",
										:id => 1972
									},
									{
										:name => "partition-all",
										:ns => "clojure.core",
										:link => "/v/1897",
										:id => 1897
									},
									{
										:name => "partition-by",
										:ns => "clojure.core",
										:link => "/v/1603",
										:id => 1603
									},
									{
										:name => "split-at",
										:ns => "clojure.core",
										:link => "/v/1724",
										:id => 1724
									},
									{
										:name => "split-with",
										:ns => "clojure.core",
										:link => "/v/1614",
										:id => 1614
									},
									{
										:name => "filter",
										:ns => "clojure.core",
										:link => "/v/1784",
										:id => 1784
									},
									{
										:name => "remove",
										:ns => "clojure.core",
										:link => "/v/2072",
										:id => 2072
									},
									{
										:name => "replace",
										:ns => "clojure.core",
										:link => "/v/1831",
										:id => 1831
									},
									{
										:name => "shuffle",
										:ns => "clojure.core",
										:link => "/v/1770",
										:id => 1770
									}
								]

							},
							{
								:name => "Use (Iteration)",
								:symbols => [
									{
										:name => "for",
										:ns => "clojure.core",
										:link => "/v/1548",
										:id => 1548
									},
									{
										:name => "doseq",
										:ns => "clojure.core",
										:link => "/v/1534",
										:id => 1534
									},
									{
										:name => "map",
										:ns => "clojure.core",
										:link => "/v/1734",
										:id => 1734
									},
									{
										:name => "map-indexed",
										:ns => "clojure.core",
										:link => "/v/1974",
										:id => 1974
									},
									{
										:name => "mapcat",
										:ns => "clojure.core",
										:link => "/v/1756",
										:id => 1756
									},
									{
										:name => "reduce",
										:ns => "clojure.core",
										:link => "/v/1868",
										:id => 1868
									},
									{
										:name => "reductions",
										:ns => "clojure.core",
										:link => "/v/1811",
										:id => 1811
									},
									{
										:name => "max-key",
										:ns => "clojure.core",
										:link => "/v/1506",
										:id => 1506
									},
									{
										:name => "min-key",
										:ns => "clojure.core",
										:link => "/v/1909",
										:id => 1909
									},
									{
										:name => "doall",
										:ns => "clojure.core",
										:link => "/v/1806",
										:id => 1806
									},
									{
										:name => "dorun",
										:ns => "clojure.core",
										:link => "/v/1526",
										:id => 1526
									}
								]

							}
						]

					},
					{
						:name => "Transients",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "transient",
										:ns => "clojure.core",
										:link => "/v/1816",
										:id => 1816
									},
									{
										:name => "persistent!",
										:ns => "clojure.core",
										:link => "/v/1970",
										:id => 1970
									}
								]

							},
							{
								:name => "Use (General)",
								:symbols => [
									{
										:name => "conj!",
										:ns => "clojure.core",
										:link => "/v/1761",
										:id => 1761
									},
									{
										:name => "pop!",
										:ns => "clojure.core",
										:link => "/v/1594",
										:id => 1594
									},
									{
										:name => "assoc!",
										:ns => "clojure.core",
										:link => "/v/1640",
										:id => 1640
									},
									{
										:name => "dissoc!",
										:ns => "clojure.core",
										:link => "/v/1853",
										:id => 1853
									},
									{
										:name => "disj!",
										:ns => "clojure.core",
										:link => "/v/2109",
										:id => 2109
									}
								]

							},
							{
								:name => "Use ('Modification')",
								:symbols => [
									{
										:name => "conj",
										:ns => "clojure.core",
										:link => "/v/1696",
										:id => 1696
									},
									{
										:name => "concat",
										:ns => "clojure.core",
										:link => "/v/1691",
										:id => 1691
									},
									{
										:name => "distinct",
										:ns => "clojure.core",
										:link => "/v/1580",
										:id => 1580
									},
									{
										:name => "group-by",
										:ns => "clojure.core",
										:link => "/v/1940",
										:id => 1940
									},
									{
										:name => "partition",
										:ns => "clojure.core",
										:link => "/v/1972",
										:id => 1972
									},
									{
										:name => "partition-all",
										:ns => "clojure.core",
										:link => "/v/1897",
										:id => 1897
									},
									{
										:name => "partition-by",
										:ns => "clojure.core",
										:link => "/v/1603",
										:id => 1603
									},
									{
										:name => "split-at",
										:ns => "clojure.core",
										:link => "/v/1724",
										:id => 1724
									},
									{
										:name => "split-with",
										:ns => "clojure.core",
										:link => "/v/1614",
										:id => 1614
									},
									{
										:name => "filter",
										:ns => "clojure.core",
										:link => "/v/1784",
										:id => 1784
									},
									{
										:name => "remove",
										:ns => "clojure.core",
										:link => "/v/2072",
										:id => 2072
									},
									{
										:name => "replace",
										:ns => "clojure.core",
										:link => "/v/1831",
										:id => 1831
									},
									{
										:name => "shuffle",
										:ns => "clojure.core",
										:link => "/v/1770",
										:id => 1770
									}
								]

							},
							{
								:name => "Use (Iteration)",
								:symbols => [
									{
										:name => "for",
										:ns => "clojure.core",
										:link => "/v/1548",
										:id => 1548
									},
									{
										:name => "doseq",
										:ns => "clojure.core",
										:link => "/v/1534",
										:id => 1534
									},
									{
										:name => "map",
										:ns => "clojure.core",
										:link => "/v/1734",
										:id => 1734
									},
									{
										:name => "map-indexed",
										:ns => "clojure.core",
										:link => "/v/1974",
										:id => 1974
									},
									{
										:name => "mapcat",
										:ns => "clojure.core",
										:link => "/v/1756",
										:id => 1756
									},
									{
										:name => "reduce",
										:ns => "clojure.core",
										:link => "/v/1868",
										:id => 1868
									},
									{
										:name => "reductions",
										:ns => "clojure.core",
										:link => "/v/1811",
										:id => 1811
									},
									{
										:name => "max-key",
										:ns => "clojure.core",
										:link => "/v/1506",
										:id => 1506
									},
									{
										:name => "min-key",
										:ns => "clojure.core",
										:link => "/v/1909",
										:id => 1909
									},
									{
										:name => "doall",
										:ns => "clojure.core",
										:link => "/v/1806",
										:id => 1806
									},
									{
										:name => "dorun",
										:ns => "clojure.core",
										:link => "/v/1526",
										:id => 1526
									}
								]

							}
						]

					}
				]

			},
			{
				:name => "Code Structure",
				:categories => [
					{
						:name => "Varibles",
						:groups => [
							{
								:name => "Create",
								:symbols => [
									{
										:name => "def",
										:ns => "clojure.core",
										:link => "/v/5268",
										:id => 5268
									},
									{
										:name => "defonce",
										:ns => "clojure.core",
										:link => "/v/2029",
										:id => 2029
									},
									{
										:name => "intern",
										:ns => "clojure.core",
										:link => "/v/1779",
										:id => 1779
									},
									{
										:name => "declare",
										:ns => "clojure.core",
										:link => "/v/1967",
										:id => 1967
									}
								]

							},
							{
								:name => "Use",
								:symbols => [
									{
										:name => "set!",
										:ns => "clojure.core",
										:link => "/v/5280",
										:id => 5280
									},
									{
										:name => "alter-var-root",
										:ns => "clojure.core",
										:link => "/v/2100",
										:id => 2100
									},
									{
										:name => "binding",
										:ns => "clojure.core",
										:link => "/v/2093",
										:id => 2093
									},
									{
										:name => "with-bindings",
										:ns => "clojure.core",
										:link => "/v/1845",
										:id => 1845
									},
									{
										:name => "with-bindings*",
										:ns => "clojure.core",
										:link => "/v/1695",
										:id => 1695
									},
									{
										:name => "with-local-vars",
										:ns => "clojure.core",
										:link => "/v/1843",
										:id => 1843
									},
									{
										:name => "letfn",
										:ns => "clojure.core",
										:link => "/v/1546",
										:id => 1546
									},
									{
										:name => "gensym",
										:ns => "clojure.core",
										:link => "/v/1529",
										:id => 1529
									}
								]

							},
							{
								:name => "Inspect",
								:symbols => [
									{
										:name => "var",
										:ns => "clojure.core",
										:link => "/v/5272",
										:id => 5272
									},
									{
										:name => "find-var",
										:ns => "clojure.core",
										:link => "/v/1762",
										:id => 1762
									},
									{
										:name => "var-get",
										:ns => "clojure.core",
										:link => "/v/1887",
										:id => 1887
									},
									{
										:name => "var?",
										:ns => "clojure.core",
										:link => "/v/1787",
										:id => 1787
									},
									{
										:name => "bound?",
										:ns => "clojure.core",
										:link => "/v/1947",
										:id => 1947
									},
									{
										:name => "resolve",
										:ns => "clojure.core",
										:link => "/v/1944",
										:id => 1944
									},
									{
										:name => "ns-resolve",
										:ns => "clojure.core",
										:link => "/v/1920",
										:id => 1920
									},
									{
										:name => "special-symbol?",
										:ns => "clojure.core",
										:link => "/v/1759",
										:id => 1759
									}
								]

							}
						]

					},
					{
						:name => "Namespaces",
						:groups => [
							{
								:name => "Create &amp; Delete",
								:symbols => [
									{
										:name => "ns",
										:ns => "clojure.core",
										:link => "/v/1794",
										:id => 1794
									},
									{
										:name => "create-ns",
										:ns => "clojure.core",
										:link => "/v/1798",
										:id => 1798
									},
									{
										:name => "remove-ns",
										:ns => "clojure.core",
										:link => "/v/1687",
										:id => 1687
									}
								]

							},
							{
								:name => "Inspect",
								:symbols => [
									{
										:name => "*ns*",
										:ns => "clojure.core",
										:link => "/v/1590",
										:id => 1590
									},
									{
										:name => "ns-name",
										:ns => "clojure.core",
										:link => "/v/1769",
										:id => 1769
									},
									{
										:name => "all-ns",
										:ns => "clojure.core",
										:link => "/v/1565",
										:id => 1565
									},
									{
										:name => "the-ns",
										:ns => "clojure.core",
										:link => "/v/1510",
										:id => 1510
									},
									{
										:name => "find-ns",
										:ns => "clojure.core",
										:link => "/v/1899",
										:id => 1899
									},
									{
										:name => "ns-publics",
										:ns => "clojure.core",
										:link => "/v/1561",
										:id => 1561
									},
									{
										:name => "ns-interns",
										:ns => "clojure.core",
										:link => "/v/1612",
										:id => 1612
									},
									{
										:name => "ns-refers",
										:ns => "clojure.core",
										:link => "/v/1726",
										:id => 1726
									},
									{
										:name => "ns-aliases",
										:ns => "clojure.core",
										:link => "/v/1508",
										:id => 1508
									},
									{
										:name => "ns-imports",
										:ns => "clojure.core",
										:link => "/v/1880",
										:id => 1880
									},
									{
										:name => "ns-map",
										:ns => "clojure.core",
										:link => "/v/1997",
										:id => 1997
									}
								]

							},
							{
								:name => "Use",
								:symbols => [
									{
										:name => "in-ns",
										:ns => "clojure.core",
										:link => "/v/1965",
										:id => 1965
									},
									{
										:name => "ns-resolve",
										:ns => "clojure.core",
										:link => "/v/1920",
										:id => 1920
									},
									{
										:name => "ns-unalias",
										:ns => "clojure.core",
										:link => "/v/1560",
										:id => 1560
									},
									{
										:name => "ns-unmap",
										:ns => "clojure.core",
										:link => "/v/1577",
										:id => 1577
									},
									{
										:name => "alias",
										:ns => "clojure.core",
										:link => "/v/2056",
										:id => 2056
									}
								]

							},
							{
								:name => "Misc",
								:symbols => [
									{
										:name => "namespace-munge",
										:ns => "clojure.core",
										:link => "/v/1936",
										:id => 1936
									},
									{
										:name => "print-namespace-doc",
										:ns => "clojure.core",
										:link => "/v/1589",
										:id => 1589
									}
								]

							}
						]

					},
					{
						:name => "Hierarchies",
						:groups => [
							{
								:name => "General",
								:symbols => [
									{
										:name => "make-hierarchy",
										:ns => "clojure.core",
										:link => "/v/1637",
										:id => 1637
									},
									{
										:name => "derive",
										:ns => "clojure.core",
										:link => "/v/1595",
										:id => 1595
									},
									{
										:name => "underive",
										:ns => "clojure.core",
										:link => "/v/1964",
										:id => 1964
									},
									{
										:name => "parents",
										:ns => "clojure.core",
										:link => "/v/1834",
										:id => 1834
									},
									{
										:name => "ancestors",
										:ns => "clojure.core",
										:link => "/v/1968",
										:id => 1968
									},
									{
										:name => "descendants",
										:ns => "clojure.core",
										:link => "/v/1641",
										:id => 1641
									},
									{
										:name => "isa?",
										:ns => "clojure.core",
										:link => "/v/1711",
										:id => 1711
									}
								]

							}
						]

					},
					{
						:name => "User Defined Types",
						:groups => [
							{
								:name => "General",
								:symbols => [
									{
										:name => "defprotocol",
										:ns => "clojure.core",
										:link => "/v/2062",
										:id => 2062
									},
									{
										:name => "defrecord",
										:ns => "clojure.core",
										:link => "/v/1647",
										:id => 1647
									},
									{
										:name => "deftype",
										:ns => "clojure.core",
										:link => "/v/1933",
										:id => 1933
									},
									{
										:name => "reify",
										:ns => "clojure.core",
										:link => "/v/1991",
										:id => 1991
									},
									{
										:name => "extend",
										:ns => "clojure.core",
										:link => "/v/1597",
										:id => 1597
									},
									{
										:name => "extend-protocol",
										:ns => "clojure.core",
										:link => "/v/1960",
										:id => 1960
									},
									{
										:name => "extend-type",
										:ns => "clojure.core",
										:link => "/v/1821",
										:id => 1821
									},
									{
										:name => "extenders",
										:ns => "clojure.core",
										:link => "/v/1807",
										:id => 1807
									}
								]

							}
						]

					},
					{
						:name => "Metadata",
						:groups => [
							{
								:name => "General",
								:symbols => [
									{
										:name => "meta",
										:ns => "clojure.core",
										:link => "/v/2110",
										:id => 2110
									},
									{
										:name => "with-meta",
										:ns => "clojure.core",
										:link => "/v/1651",
										:id => 1651
									},
									{
										:name => "vary-meta",
										:ns => "clojure.core",
										:link => "/v/1669",
										:id => 1669
									},
									{
										:name => "reset-meta!",
										:ns => "clojure.core",
										:link => "/v/1910",
										:id => 1910
									},
									{
										:name => "alter-meta!",
										:ns => "clojure.core",
										:link => "/v/1788",
										:id => 1788
									}
								]

							}
						]

					}
				]

			},
			{
				:name => "Environment",
				:categories => [
					{
						:name => "Require / Import",
						:groups => [
							{
								:name => "General",
								:symbols => [
									{
										:name => "use",
										:ns => "clojure.core",
										:link => "/v/2057",
										:id => 2057
									},
									{
										:name => "require",
										:ns => "clojure.core",
										:link => "/v/1928",
										:id => 1928
									},
									{
										:name => "import",
										:ns => "clojure.core",
										:link => "/v/1621",
										:id => 1621
									},
									{
										:name => "refer-clojure",
										:ns => "clojure.core",
										:link => "/v/1665",
										:id => 1665
									},
									{
										:name => "refer",
										:ns => "clojure.core",
										:link => "/v/1963",
										:id => 1963
									}
								]

							}
						]

					},
					{
						:name => "Code",
						:groups => [
							{
								:name => "General",
								:symbols => [
									{
										:name => "*compile-files*",
										:ns => "clojure.core",
										:link => "/v/1655",
										:id => 1655
									},
									{
										:name => "*compile-path*",
										:ns => "clojure.core",
										:link => "/v/1505",
										:id => 1505
									},
									{
										:name => "*file*",
										:ns => "clojure.core",
										:link => "/v/1772",
										:id => 1772
									},
									{
										:name => "*warn-on-reflection*",
										:ns => "clojure.core",
										:link => "/v/1680",
										:id => 1680
									},
									{
										:name => "compile",
										:ns => "clojure.core",
										:link => "/v/1914",
										:id => 1914
									},
									{
										:name => "load",
										:ns => "clojure.core",
										:link => "/v/1857",
										:id => 1857
									},
									{
										:name => "load-file",
										:ns => "clojure.core",
										:link => "/v/1984",
										:id => 1984
									},
									{
										:name => "load-reader",
										:ns => "clojure.core",
										:link => "/v/1822",
										:id => 1822
									},
									{
										:name => "load-string",
										:ns => "clojure.core",
										:link => "/v/1939",
										:id => 1939
									},
									{
										:name => "read",
										:ns => "clojure.core",
										:link => "/v/1635",
										:id => 1635
									},
									{
										:name => "read-string",
										:ns => "clojure.core",
										:link => "/v/1707",
										:id => 1707
									},
									{
										:name => "gen-class",
										:ns => "clojure.core",
										:link => "/v/2021",
										:id => 2021
									},
									{
										:name => "gen-interface",
										:ns => "clojure.core",
										:link => "/v/1573",
										:id => 1573
									},
									{
										:name => "loaded-libs",
										:ns => "clojure.core",
										:link => "/v/1948",
										:id => 1948
									},
									{
										:name => "test",
										:ns => "clojure.core",
										:link => "/v/1796",
										:id => 1796
									}
								]

							}
						]

					},
					{
						:name => "IO",
						:groups => [
							{
								:name => "General",
								:symbols => [
									{
										:name => "*in*",
										:ns => "clojure.core",
										:link => "/v/2045",
										:id => 2045
									},
									{
										:name => "*out*",
										:ns => "clojure.core",
										:link => "/v/1721",
										:id => 1721
									},
									{
										:name => "*err*",
										:ns => "clojure.core",
										:link => "/v/1854",
										:id => 1854
									},
									{
										:name => "print",
										:ns => "clojure.core",
										:link => "/v/1668",
										:id => 1668
									},
									{
										:name => "printf",
										:ns => "clojure.core",
										:link => "/v/1918",
										:id => 1918
									},
									{
										:name => "println",
										:ns => "clojure.core",
										:link => "/v/2015",
										:id => 2015
									},
									{
										:name => "pr",
										:ns => "clojure.core",
										:link => "/v/2017",
										:id => 2017
									},
									{
										:name => "prn",
										:ns => "clojure.core",
										:link => "/v/1650",
										:id => 1650
									},
									{
										:name => "print-str",
										:ns => "clojure.core",
										:link => "/v/1716",
										:id => 1716
									},
									{
										:name => "println-str",
										:ns => "clojure.core",
										:link => "/v/2081",
										:id => 2081
									},
									{
										:name => "pr-str",
										:ns => "clojure.core",
										:link => "/v/1859",
										:id => 1859
									},
									{
										:name => "prn-str",
										:ns => "clojure.core",
										:link => "/v/1748",
										:id => 1748
									},
									{
										:name => "newline",
										:ns => "clojure.core",
										:link => "/v/1681",
										:id => 1681
									},
									{
										:name => "flush",
										:ns => "clojure.core",
										:link => "/v/2087",
										:id => 2087
									},
									{
										:name => "read-line",
										:ns => "clojure.core",
										:link => "/v/1495",
										:id => 1495
									},
									{
										:name => "slurp",
										:ns => "clojure.core",
										:link => "/v/1753",
										:id => 1753
									},
									{
										:name => "spit",
										:ns => "clojure.core",
										:link => "/v/1555",
										:id => 1555
									},
									{
										:name => "with-in-str",
										:ns => "clojure.core",
										:link => "/v/1602",
										:id => 1602
									},
									{
										:name => "with-out-str",
										:ns => "clojure.core",
										:link => "/v/1652",
										:id => 1652
									},
									{
										:name => "with-open",
										:ns => "clojure.core",
										:link => "/v/1567",
										:id => 1567
									}
								]

							}
						]

					},
					{
						:name => "REPL",
						:groups => [
							{
								:name => "General",
								:symbols => [
									{
										:name => "*1",
										:ns => "clojure.core",
										:link => "/v/1746",
										:id => 1746
									},
									{
										:name => "*2",
										:ns => "clojure.core",
										:link => "/v/1549",
										:id => 1549
									},
									{
										:name => "*3",
										:ns => "clojure.core",
										:link => "/v/1531",
										:id => 1531
									},
									{
										:name => "*e",
										:ns => "clojure.core",
										:link => "/v/1717",
										:id => 1717
									},
									{
										:name => "*print-dup*",
										:ns => "clojure.core",
										:link => "/v/2020",
										:id => 2020
									},
									{
										:name => "*print-length*",
										:ns => "clojure.core",
										:link => "/v/1741",
										:id => 1741
									},
									{
										:name => "*print-level*",
										:ns => "clojure.core",
										:link => "/v/1607",
										:id => 1607
									},
									{
										:name => "*print-meta*",
										:ns => "clojure.core",
										:link => "/v/2041",
										:id => 2041
									},
									{
										:name => "*print-readably*",
										:ns => "clojure.core",
										:link => "/v/2037",
										:id => 2037
									}
								]

							}
						]

					},
					{
						:name => "Misc",
						:groups => [
							{
								:name => "General",
								:symbols => [
									{
										:name => "*clojure-version*",
										:ns => "clojure.core",
										:link => "/v/1786",
										:id => 1786
									},
									{
										:name => "clojure-version",
										:ns => "clojure.core",
										:link => "/v/2019",
										:id => 2019
									},
									{
										:name => "*command-line-args*",
										:ns => "clojure.core",
										:link => "/v/1672",
										:id => 1672
									},
									{
										:name => "time",
										:ns => "clojure.core",
										:link => "/v/1527",
										:id => 1527
									}
								]

							}
						]

					}
				]

			}
		]
	end
end