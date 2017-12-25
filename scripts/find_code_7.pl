#!/usr/bin/perl

# use strict;
# use warnings;
# no warnings 'recursion';

use Memoize;
memoize('secret', TIE => [ Memoize::ExpireLRU, CACHESIZE => 10000 ]);

use Const::Fast;

const my $lower => 25_734;
const my $minus => 32_767;
const my $mod   => 32_768;

my $result = 0;

for my $i ( $lower .. $minus ) {
    $result = secret(4, 1, $i);
    printf "%05d\t%05d\t%s\n", $i, $result, $result == 6 ? '*** SUCCESS ***' : '-';
    last if $result == 6;
}

print $result, "\n";

sub secret {
    my ($a, $b, $r7) = @_;

    if ($a == 0) {
        $a = ($b + 1) % $mod;
        print $a, ' ', $b, ' ', $r7, "\n" if $a == 6;
        return $a;
    }
    
    if ($b == 0) {
        $a = ($a + $minus) % $mod;
        return secret($a, $r7, $r7);
    }

    $b = secret($a, ($b + $minus) % $mod, $r7);
    $a = ($a + $minus) % $mod;
    return secret($a, $b, $r7);
}
