package igrek.robopath.mazegenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import igrek.robopath.common.Point;
import igrek.robopath.common.tilemap.TileMap;

@Component
public class MazeGenerator {
	
	private final Random random;
	
	@Autowired
	public MazeGenerator(Random random) {
		this.random = random;
	}
	
	public void generateMaze(TileMap map) {
		// set all blocked
		for (int x = 0; x < map.getWidthInTiles(); x++) {
			for (int y = 0; y < map.getHeightInTiles(); y++) {
				map.setCell(x, y, true);
			}
		}
		//dwie pomocnicze listy do wygenerowania labiryntu
		List<Point> unvisited = new ArrayList<>(); //lista nieodwiedzonych punktów w labiryncie do połączenia z odwiedzonymi punktami
		List<Point> visited = new ArrayList<>(); //lista odwiedzonych i należących już do labiryntu
		//dodaj co drugi punkt do listy odwiedzonych
		for (int x = 0; x < map.getWidthInTiles(); x += 2) {
			for (int y = 0; y < map.getHeightInTiles(); y += 2) {
				unvisited.add(new Point(x, y));
				map.setCell(x, y, false); //wypełnij pustym polem
			}
		}
		//wylosuj jeden punkt z listy odwiedzonych - początek rozrostu labiryntu
		int index = random.nextInt(unvisited.size()); //losuj numer od 0 do (liczba elementów na liście nieodwiedzonych)
		Point current = unvisited.get(index); //wyciągnij element o tym numerze z listy
		//i przepisz go z listy nieodwiedzonych do listy odwiedzonych
		visited.add(current);
		unvisited.remove(current);
		//połączenie punktów nieodwiedzonych z punktami odwiedzonymi
		while (!unvisited.isEmpty()) { //dopóki są jeszcze nieodwiedzone punkty do połączenia
			//wybierz pierwszy punkt: wylosuj dowolny punkt z listy nieodwiedzonych
			index = random.nextInt(unvisited.size());
			Point p1 = unvisited.get(index);
			//połącz z najbliższym punktem na liście odwiedzonych
			//wybierz drugi punkt jako wyznaczonego najbliższego sąsiada z listy odwiedzonych
			Point p2 = getClosestFromVisited(p1, visited);
			//połącz ze sobą wybrane 2 punkty: p1, p2
			int p1x = p1.x; //aktualna pozycja w drodze między punktami
			int p1y = p1.y;
			while (p1x < p2.x) { //idź w prawo
				map.setCell(p1x, p1y, false); //wyburzenie ściany
				p1x++;
			}
			while (p1x > p2.x) { //idź w lewo
				map.setCell(p1x, p1y, false); //wyburzenie ściany
				p1x--;
			}
			while (p1y < p2.y) { //idź w dół
				map.setCell(p1x, p1y, false); //wyburzenie ściany
				p1y++;
			}
			while (p1y > p2.y) { //idź w górę
				map.setCell(p1x, p1y, false); //wyburzenie ściany
				p1y--;
			}
			//przepisz punkt z listy nieodwiedzonych do odwiedzonych
			visited.add(p1);
			unvisited.remove(p1);
		}
	}
	
	private Point getClosestFromVisited(Point p1, List<Point> visited) {
		Point minP = visited.get(0);
		for (Point p2 : visited) {
			if (distance(p1, p2) < distance(p1, minP)) // new minimum
				minP = p2;
		}
		return minP;
	}
	
	private int distance(Point p1, Point p2) {
		// Manhattan metrics
		return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
	}
}
