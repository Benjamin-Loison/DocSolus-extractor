#include <iostream>
#include <vector>
#include <string>
#include <dirent.h>
#include <unistd.h>
#include <stdio.h>
#include <sys/stat.h>
#include <fstream>
#include <Magick++.h>

#define UNSIGNED_SHORT_MAX 65535

using namespace Magick;
using namespace std;

string currentWorkingDirectory = "";

vector<string> listFiles(string directory) // also list folders
{
	vector<string> files;
	DIR *d;
    struct dirent *dir;
    d = opendir(directory.c_str());
    if(d)
    {
        while((dir = readdir(d)) != NULL)
        {
            string fileName = dir->d_name;
            if(fileName != "." && fileName != "..")
                files.push_back(directory + fileName);
        }
        closedir(d);
    }
    return files;
}

string getCurrentWorkingDir()
{
	if(currentWorkingDirectory == "")
	{
		char buff[FILENAME_MAX];
		getcwd(buff, FILENAME_MAX);
		currentWorkingDirectory = string(buff) + "/"; // TODO: if Windows should use ifdef to make cross plateform or use parameter already in headers
	}
	return currentWorkingDirectory;
}

bool createDirectory(string path) // Linux oriented source code
{
	if(mkdir(path.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH) == -1)
    {
        cout << "Error while creating folder: " + path << endl;
        return false;
    }
	return true;
}

void print(string toPrint)
{
	cout << toPrint << endl;
}

vector<string> split(string s, string delimiter)
{
    vector<string> toReturn;
    size_t pos = 0;
    while((pos = s.find(delimiter)) != string::npos)
    {
        toReturn.push_back(s.substr(0, pos));
        s.erase(0, pos + delimiter.length());
    }
    toReturn.push_back(s);
    return toReturn;
}

void copyFromTo(string from, string to)
{
	ifstream src(from, ios::binary);
	ofstream dst(to, ios::binary);
	dst << src.rdbuf();
}

int main(int argc, char **argv)
{
	try
	{
		InitializeMagick(*argv);
		string folder = "pic/";
		vector<string> folderNames = listFiles(folder);
		unsigned short folderNamesSize = folderNames.size(), folderNamesSizeMinus1 = folderNamesSize - 1;
		for(unsigned short folderIndex = 0; folderIndex < folderNamesSize; folderIndex++)
		{
			// assuming folder
			string folderName = folderNames[folderIndex] + "/", folderPath = getCurrentWorkingDir() + folderName;
			cout << "Working on folder: " << folderPath << " (" << folderIndex << " / " << folderNamesSizeMinus1 << ")" << endl;
			vector<string> fileNames = listFiles(folderName);
			unsigned short fileNamesSize = fileNames.size(), fileNamesSizeMinus1 = fileNamesSize - 1;
			for(unsigned short fileIndex = 0; fileIndex < fileNamesSize; fileIndex++)
			{
				string fileName = fileNames[fileIndex], filePath = getCurrentWorkingDir() + fileName;
				cout << "Working on file: " << filePath << " (" << fileIndex << " / " << fileNamesSizeMinus1 << ")" << endl;
				Image img(filePath);
				unsigned short width = img.baseColumns(), height = img.baseRows(); // check difference before doing
				unsigned short mini = height - 1;
				for(short y = height - 1; y > -1; y--)
				{
					unsigned short x = 0;
					for(; x < width; x++)
					{
						//cout << x << " " << y << endl;
						ColorRGB rgb(img.pixelColor(x, y));
						if(rgb.red() != rgb.blue() || rgb.blue() != rgb.green() || rgb.blue() != 0)
						{
							break;
						}
					}
					//if(x != 0)
					//	cout << x << " " << endl;
					if(x == width)
					{
						mini = y;
					}
				}
				unsigned short difference = height - mini;
				if(difference != 0)
				{
					cout << height - mini << endl;
					//remove(filePath.c_str());
					img.crop(Geometry(width, height - difference - 1, 0, 0));
					img.write(filePath);
				}
			}
		}
	}
	catch(Magick::Exception & error)
	{
		cerr << "Caught Magick++ exception: " << error.what() << endl;
	}
 	return 0;
}
